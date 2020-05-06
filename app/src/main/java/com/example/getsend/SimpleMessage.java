package com.example.getsend;

    // Message.java
    public class SimpleMessage {
        private String text;
        private int type;

        public SimpleMessage(String text, int type) {
            this.text = text;
            this.type = type;
        }

        public SimpleMessage(){

        }

        public String getText() {
            return text;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
}
