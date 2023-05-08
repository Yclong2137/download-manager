package com.autolink.app.downloadmanager;

/**
 * 下载请求
 */
public class Request {

    private String id;

    private Request(Builder builder) {
        id = builder.id;
    }

    public static class Builder {

        private String id;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Request build() {
            return new Request(this);
        }

    }

    public String getId() {
        return id;
    }
}
