package com.xfjr.common.util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

public class EncryptUtil {
    private static final Logger log = LoggerFactory.getLogger(EncryptUtil.class);


    public static String encodeBase64(byte[] bytes){
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return encoded;
    }

    public static byte[]  decodeBase64(String str){
        byte[] bytes = null;
        bytes = Base64.getDecoder().decode(str);
        return bytes;
    }

    public static String encodeUTF8StringBase64(String str){
        String encoded = null;
        try {
            encoded = Base64.getEncoder().encodeToString(str.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return encoded;

    }

    public static String  decodeUTF8StringBase64(String str){
        String decoded = null;
        byte[] bytes = Base64.getDecoder().decode(str);
        try {
            decoded = new String(bytes,"utf-8");
        }catch(UnsupportedEncodingException e){
            log.error(e.getMessage(), e);
        }
        return decoded;
    }

    public static String encodeURL(String url) {
    	String encoded = null;
		try {
			encoded =  URLEncoder.encode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return encoded;
	}


	public static String decodeURL(String url) {
    	String decoded = null;
		try {
			decoded = URLDecoder.decode(url, "utf-8");
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}
		return decoded;
	}

    public static void main(String [] args){
        String str = "abcd{'a':'b'}";
        String encoded = EncryptUtil.encodeUTF8StringBase64(str);
        String decoded = EncryptUtil.decodeUTF8StringBase64(encoded);
        log.info(str);
        log.info(encoded);
        log.info(decoded);

        String url = "== wo";
        String urlEncoded = EncryptUtil.encodeURL(url);
        String urlDecoded = EncryptUtil.decodeURL(urlEncoded);
        
        log.info(url);
        log.info(urlEncoded);
        log.info(urlDecoded);
    }


}
