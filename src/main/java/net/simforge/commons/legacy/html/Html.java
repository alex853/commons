package net.simforge.commons.legacy.html;

public class Html {

    public static String toPlainText(String html) {
        String result = "";
        while (true) {
            Token t = nextToken(html);
            if (t == null)
                break;
            html = remove(html, t);

            if (t.tag) {
                if (t.tagName.equals("br")) {
                    result += "\r\n";
                } else if (t.tagName.equals("/td")) {
                    Token next = nextToken(html);
                    if (next.tag && next.tagName.equals("td"))
                        result += ";";
                } else if (t.tagName.equals("/tr")) {
                    result += "\r\n";
                }
            } else {
                result += t.html;
            }
        }

        while (true) {
            int i = result.indexOf("&nbsp;");
            if (i == -1) {
                break;
            }
            result = result.substring(0, i) + " " + result.substring(i + "&nbsp;".length());
        }

        return result;
    }

    private static String remove(String html, Token t) {
        return html.substring(t.html.length());
    }

    private static Token nextToken(String html) {
        if (html.length() == 0)
            return null;

        int i = html.indexOf('<');
        if (i == 0) {
            int j = html.indexOf('>');
            if (j == -1)
                throw new IllegalStateException();

            String fullTag = html.substring(1, j).trim();
            String tagName = "";
            for (int k = 0; k < fullTag.length(); k++) {
                char c = fullTag.charAt(k);
                if (Character.isLetter(c) || c == '!' || c == '/') {
                    tagName += c;
                } else {
                    break;
                }
            }

            Token t = new Token();
            t.tag = true;
            t.tagName = tagName;
            t.html = html.substring(0, j + 1);

            return t;
        } else {
            Token t = new Token();
            t.tag = false;
            t.html = (i == -1) ? html : html.substring(0, i);
            return t;
        }
    }

    private static class Token {
        boolean tag;
        String tagName;
        String html;
    }
}
