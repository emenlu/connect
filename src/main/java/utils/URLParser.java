package utils;

public class URLParser {
	/**
	 * Extract the first URL from the given string.
	 * @return "" if no url is found, url otherwise
	 */
	public static String find(String content) {
		int http = content.indexOf("http://");
		int https = content.indexOf("https://");
		
		if (http == -1 && https == -1)
			return "";

		int start = http >= 0 ? http : https;
		int end = content.indexOf(" ", start);
        if (end == -1)
            end = content.length();
            
		return content.substring(start, end);
	}
}
