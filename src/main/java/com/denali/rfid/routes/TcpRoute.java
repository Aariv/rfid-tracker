package com.denali.rfid.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.denali.rfid.processors.DataProcessor;

@Component
public class TcpRoute extends RouteBuilder {

	private static final Logger log = LoggerFactory.getLogger(TcpRoute.class);

	@Autowired
	private DataProcessor processor;

	@Override
	public void configure() throws Exception {
		 from("netty4:tcp://0.0.0.0:4001?textline=true&clientMode=false&decoderMaxLineLength=262144000")
		 .setHeader(Exchange.FILE_NAME, constant("stream.txt")).process(processor)
		 .to("file:outbox?fileExist=Append");

//		from("netty4:tcp://0.0.0.0:4001?textline=true&clientMode=false&decoderMaxLineLength=262144000").to("seda:stage?concurrentConsumers=2");
//		from("seda:stage").setHeader(Exchange.FILE_NAME, constant("stream1.txt")).process(processor)
//				.to("file:outbox1?fileExist=Append");

	}
}