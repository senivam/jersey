/*
 * Copyright (c) 2023, 2025 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.netty.connector;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedInput;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Promise;

import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JerseyExpectContinueHandler extends ChannelDuplexHandler {

    private ExpectationState currentState = ExpectationState.IDLE;

    private static final List<HttpResponseStatus> finalErrorStatuses = Arrays.asList(HttpResponseStatus.UNAUTHORIZED,
            HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE);
    private static final List<HttpResponseStatus> reSendErrorStatuses = Arrays.asList(
            HttpResponseStatus.METHOD_NOT_ALLOWED,
            HttpResponseStatus.EXPECTATION_FAILED);

    private static final List<HttpResponseStatus> errorStatuses = new ArrayList<>(finalErrorStatuses);
    private static final List<HttpResponseStatus> statusesToBeConsidered = new ArrayList<>(reSendErrorStatuses);

    static {
        errorStatuses.addAll(reSendErrorStatuses);
        statusesToBeConsidered.addAll(finalErrorStatuses);
        statusesToBeConsidered.add(HttpResponseStatus.CONTINUE);
    }

    private HttpResponseStatus status = null;

    private CompletableFuture<NettyConnector.ExchangePair<Boolean, Exception>> exchanger;

    private HttpRequest originalNettyRequest;
    private HttpContent originalContent;
    private ChunkedInput originalChunkedContent;

    private Promise continuePromise;

    private boolean propagateLastMessage = false;

    public JerseyExpectContinueHandler(CompletableFuture<NettyConnector.ExchangePair<Boolean, Exception>> exchanger) {
        this.exchanger = exchanger;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        System.out.println("WRITING MESSAGE TO CHANNEL: " + msg);
        System.out.println("INSTANCE OF: " + (msg instanceof ChunkedInput));
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {
                originalNettyRequest = request;
                currentState = ExpectationState.SENT;
                // Write request headers only, content will be sent later
                super.write(ctx, request, promise);
                return;
            }
        }
        if ((msg instanceof ChunkedInput || msg instanceof HttpContent)
                && (originalContent == null || originalChunkedContent == null)) {
            // Store the content to be sent after receiving 100 Continue
            if (currentState == ExpectationState.SENT || currentState == ExpectationState.AWAITING) {

                if (msg instanceof HttpContent) {
                    originalContent = (HttpContent) msg;
                }
                if (msg instanceof ChunkedInput) {
                    originalChunkedContent = (HttpChunkedInput) msg;
                }
                continuePromise = ctx.executor().newPromise();

                // Create timeout for 100-continue wait
                ctx.executor().schedule(() -> {
                    if (!continuePromise.isDone()) {
                        // Server didn't respond with 100 Continue in time
                        // Send the content anyway according to HTTP specs
                        sendPendingContent(ctx);
                    }
                }, 500, TimeUnit.SECONDS);

                return;
            }
        }

        super.write(ctx, msg, promise);
    }

    private void sendPendingContent(ChannelHandlerContext ctx) {
        System.out.println("SENDING ORIGINAL CONTENT: " + originalContent);
        System.out.println("SENDING ORIGINAL CHUNKED CONTENT: " + originalChunkedContent);

        if (originalContent != null || originalChunkedContent != null) {
            if (originalContent != null) {
                ctx.writeAndFlush(originalContent);
                originalContent = null;
            }
            if (originalChunkedContent != null) {
                ctx.writeAndFlush(originalChunkedContent);
                originalChunkedContent = null;
            }

            if (continuePromise != null) {
                continuePromise.setSuccess(null);
            }
        }
    }

/*

    private Promise<Void> continuePromise;
    private HttpContent pendingContent;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;

            if (HttpUtil.is100ContinueExpected(request)) {

                // Write request headers only, content will be sent later
                super.write(ctx, request, promise);
                return;
            }
        } else if (msg instanceof HttpContent && pendingContent == null) {
            // Store the content to be sent after receiving 100 Continue
            HttpRequest request = (HttpRequest) ctx.channel().attr(AttributeKey.valueOf("REQUEST")).get();
            if (request != null && HttpHeaderValues.CONTINUE.contentEqualsIgnoreCase(
                    request.headers().get(HttpHeaderNames.EXPECT))) {

                pendingContent = (HttpContent) msg;
                continuePromise = ctx.executor().newPromise();

                // Create timeout for 100-continue wait
                ctx.executor().schedule(() -> {
                    if (!continuePromise.isDone()) {
                        // Server didn't respond with 100 Continue in time
                        // Send the content anyway according to HTTP specs
                        sendPendingContent(ctx);
                    }
                }, 5, TimeUnit.SECONDS);

                return;
            }
        }


        // For any other messages, pass them through
        super.write(ctx, msg, promise);
    }



*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("READING MESSAGE: " + msg);
        if (checkExpectResponse(msg)) {
            currentState = ExpectationState.AWAITING;
        }
        switch (currentState) {
            case AWAITING:
                final HttpResponse response = (HttpResponse) msg;
                status = response.status();

                boolean handshakeDone = processErrorStatuses(status) || msg instanceof FullHttpMessage;
                currentState = (handshakeDone) ? ExpectationState.IDLE : ExpectationState.FINISHING;
                if (status == HttpResponseStatus.CONTINUE) {
                    System.out.println("YES, WE ARE CONTINUING");
                    sendPendingContent(ctx);
                }
                ctx.executor().schedule(() -> {
                            processExpectationStatus();
                        }, 500, TimeUnit.MILLISECONDS
                );
                return;
            case FINISHING:
                if (msg instanceof LastHttpContent) {
                    currentState = ExpectationState.IDLE;
                    if (propagateLastMessage) {
                        propagateLastMessage = false;
                        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
                    }
                }
                return;
            default:
                System.out.println("DEFAULT MESSAGE: " + msg);
                ctx.fireChannelRead(msg); //bypass the message to the next handler in line
        }
    }

    private boolean checkExpectResponse(Object msg) {
        if (currentState == ExpectationState.SENT && msg instanceof HttpResponse) {
            return statusesToBeConsidered.contains(((HttpResponse) msg).status());
        }
        return false;
    }

    private boolean processErrorStatuses(HttpResponseStatus status)
            throws InterruptedException {
        if (reSendErrorStatuses.contains(status)) {
            propagateLastMessage = true;
        }
        return (finalErrorStatuses.contains(status));
    }

    private void processExpectationStatus()  {
            if (status == null) {
                exchanger.completeExceptionally(new TimeoutException());
                // continue without expectations
            }
            if (!statusesToBeConsidered.contains(status)) {
                exchanger.completeExceptionally(new ProcessingException(LocalizationMessages
                        .UNEXPECTED_VALUE_FOR_EXPECT_100_CONTINUE_STATUSES(status.code()), null));
            }

            if (finalErrorStatuses.contains(status)) {
                exchanger.completeExceptionally(new IOException(LocalizationMessages
                        .EXPECT_100_CONTINUE_FAILED_REQUEST_FAILED()));
            }

            if (reSendErrorStatuses.contains(status)) {
                exchanger.completeExceptionally(new TimeoutException(LocalizationMessages
                        .EXPECT_100_CONTINUE_FAILED_REQUEST_SHOULD_BE_RESENT()));
                // Re-send request without expectations
            }

            exchanger.complete(new NettyConnector.ExchangePair<>(true, null));

    }

    private enum ExpectationState {

        SENT,
        AWAITING,
        FINISHING,
        IDLE
    }
}
