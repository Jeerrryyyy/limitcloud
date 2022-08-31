package com.jevzo.limitcloud.master.network.web

import com.jevzo.limitcloud.library.network.helper.NettyHelper
import com.jevzo.limitcloud.library.network.web.AbstractWebServer
import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpRequestDecoder
import io.netty.handler.codec.http.HttpResponseEncoder

class WebServer(
    nettyHelper: NettyHelper
) : AbstractWebServer(nettyHelper) {

    override fun preparePipeline(channel: Channel) {
        channel.pipeline()
            .addLast(HttpRequestDecoder(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, false))
            .addLast(HttpObjectAggregator(Integer.MAX_VALUE))
            .addLast(HttpResponseEncoder())
            .addLast(WebHandler())
    }
}