package org.easyproxy.handler.http;/**
 * Description : 
 * Created by YangZH on 16-8-14
 *  下午10:28
 */

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.ipfilter.IpFilterRuleType;
import io.netty.handler.ipfilter.IpSubnetFilterRule;
import org.easyproxy.constants.Const;
import org.easyproxy.util.Config;

import java.util.List;

/**
 * Description :
 * Created by YangZH on 16-8-14
 * 下午10:28
 */

public class BaseServerChildHandler extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        boolean isLogOpen = Boolean.valueOf(Config.getString(Const.LOGOPEN));
        boolean isApiOpen = Boolean.valueOf(Config.getString(Const.APIOPEN));
        boolean isProxy = Boolean.valueOf(Config.getString(Const.PROXY_SERVER));
        boolean isAntileechOpen = Boolean.valueOf(Config.getString(Const.ANTILEECH_OPEN));
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("compress", new HttpContentCompressor(9));
        pipeline.addLast("decompress", new HttpContentDecompressor());
        pipeline.addLast("aggregator", new HttpObjectAggregator(1024000));
        pipeline.addLast(new IPFilterHandler(getForbiddenList()));
        if (isLogOpen){
            pipeline.addLast(new AccessLogHandler());
        }
        if (isAntileechOpen){
            pipeline.addLast(new AntiLeechHandler());
        }
        if (isApiOpen){
            pipeline.addLast(new APIHandler());
        }
        pipeline.addLast(new PersonalPageHandler());
        if (isProxy){
            pipeline.addLast(new GetRequestHandler());
            pipeline.addLast(new PostRequestHandler());
            pipeline.addLast(new PutRequestHandler());
            pipeline.addLast(new DeleteRequestHandler());
        }
    }

    private IpSubnetFilterRule[] getForbiddenList(){
        List<String> forbidden_hosts = Config.getForbiddenHosts();
        IpSubnetFilterRule [] rules = new IpSubnetFilterRule[forbidden_hosts.size()];
        for (int index=0;index<forbidden_hosts.size();index++){
            rules[index] = new IpSubnetFilterRule(forbidden_hosts.get(index),32, IpFilterRuleType.REJECT);
        }
        System.out.println("forbidden list:"+forbidden_hosts);
        return rules;
    }
}
