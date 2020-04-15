package com.DanielDv99;

class Environment {
    private Field[] fields;

    public Environment() {
        initialize(3);
    }

    private Environment(Field[] fields) {
        this.fields = fields;
    }

    // Shared code for several constructors
    private void initialize(int nFields) {
        this.fields = new Field[nFields];
        for (int i = 0; i < nFields; i++) {
            this.fields[i] = new Field();
        }
    }

    public Field getFieldByIndex(int index) {
        // decrement index because fields indexing starts with 1, as per task description
        return fields[index - 1];
    }

    public int getFieldValue(int index) {
        var field = this.getFieldByIndex(index);
        return field.getValue();
    }

    public void updateFields(int player1Move, int player2Move) {
        if (player1Move == player2Move) {
            getFieldByIndex(player1Move).decreaseValue();
        }
        else {
            getFieldByIndex(player1Move).decreaseValue();
            getFieldByIndex(player2Move).decreaseValue();
        }
        for (int i = 0; i < fields.length; i++) {
            var fieldIndex = i + 1;
            if (fieldIndex != player1Move && fieldIndex != player2Move) {
                getFieldByIndex(fieldIndex).increaseValue();
            }
        }
    }

    @Override
    public String toString() {
        var headers = new String[this.fields.length];
        var fieldValues = new String[this.fields.length];

        var currLetter = 'A';
        for (int i = 0; i < headers.length; i++) {
            headers[i] = Character.toString(currLetter);
            fieldValues[i] = Integer.toString(this.fields[i].getValue());
            currLetter++;
        }

        return Logger.asTable(headers, fieldValues);
    }

    @Override
    public Environment clone() {
        var newFields = fields.clone();
        for (int i = 0; i < newFields.length; i++) {
            newFields[i] = newFields[i].clone();
        }

        return new Environment(newFields);
    }

    private static class Field {
        private int value;

        public Field() {
            this.value = 1;
        }

        private Field(int initValue) {
            this.value = initValue;
        }

        public int getValue() {
            return value;
        }

        public void increaseValue() {
            this.value += 1;
        }

        public void decreaseValue() {
            this.value -= 1;
            this.value = Math.max(this.value, 0);
        }

        @Override
        public Field clone() {
            return new Field(this.value);
        }
    }
}