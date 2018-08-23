package com.denali.rfid.processors;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.denali.rfid.utils.HexadecimalToBinary;
import com.denali.rfid.utils.StringTool;

import io.netty.handler.codec.DecoderException;


/**
 * @author zentere
 *
 */
@Component
public class DataProcessor implements Processor {

	private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {
		log.info("In DataProcessor");
		ByteBuffer inBuf = exchange.getIn().getBody(ByteBuffer.class);
		byte[] byteArray = inBuf.array();
		//byte[] byteArray = inBuf.array();
		String payload = StringTool.byteArrayToString(byteArray, 0, byteArray.length);
		String dataWithoutSpace = StringUtils.deleteWhitespace(payload);
		try {
			// Contains A013
			if (dataWithoutSpace.startsWith("A013")) {
				// For valid data
				String header = dataWithoutSpace.substring(0, 4);
				String readerId = dataWithoutSpace.substring(4, 6);
				String command = dataWithoutSpace.substring(6, 8);
				String antenna = dataWithoutSpace.substring(8, 10);
				String pc = dataWithoutSpace.substring(10, 12);
				// String antena = dataWithoutSpace.substring(12, 14);
				String epc = dataWithoutSpace.substring(14, 34);
				String rssi = dataWithoutSpace.substring(37, 41);
				String antennaID = "";
				
				if (HexadecimalToBinary.isHexadecimalNumber(antenna)) {
					String binary = HexadecimalToBinary.getBinaryFromHexadecimalNumber(antenna);
					String substring = binary.substring(Math.max(binary.length() - 2, 0));
					if (substring.equalsIgnoreCase("00")) {
						antennaID = "ANT1";
					} else if (substring.equalsIgnoreCase("01")) {
						antennaID = "ANT2";
					} else if (substring.equalsIgnoreCase("10")) {
						antennaID = "ANT3";
					} else if (substring.equalsIgnoreCase("11")) {
						antennaID = "ANT4";
					}
				}
				List<String> completeData = new ArrayList<>();
				completeData.add(header);
				completeData.add(readerId);
				completeData.add(command);
				completeData.add(antenna);
				completeData.add("(" + antennaID + ")");
				completeData.add(pc);
				completeData.add(epc);
				String formattedData = new String(Hex.decodeHex(epc.toCharArray()));
				completeData.add("(" + formattedData + ")");
				completeData.add(rssi);
				// Check EPC data is correct
				if (!isStringContainsSpecialChars(formattedData)) {
					log.info("Payload {}", payload);
					exchange.getIn().setBody(String.format("%s%n", payload));
				}
			} else {
				log.info("No Data");
			}
		} catch (StringIndexOutOfBoundsException | DecoderException e1) {
			log.debug("Error in Converting Byte Array to Text: {} ", e1);
		}
	}

	private boolean isStringContainsSpecialChars(String str) {
		Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");
		Matcher matcher = pattern.matcher(str);
		if (!matcher.matches()) {
			// System.out.println("string '" + str + "' contains special character");
			return true;
		} else {
			// System.out.println("string '" + str + "' doesn't contains special
			// character");
			return false;
		}
	}
}