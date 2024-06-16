package me.kubbidev.multiversus.command.util;

import me.kubbidev.multiversus.command.abstraction.Command;
import me.kubbidev.multiversus.command.abstraction.CommandException;
import me.kubbidev.multiversus.locale.Message;
import me.kubbidev.multiversus.sender.Sender;

public abstract class ArgumentException extends CommandException {

    public static class DetailedUsage extends ArgumentException {
        @Override
        protected void handle(Sender sender) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(Sender sender, String label, Command<?> command) {
            command.sendDetailedUsage(sender, label);
        }
    }

    public static class PastDate extends ArgumentException {
        @Override
        protected void handle(Sender sender) {
            Message.PAST_DATE_ERROR.send(sender);
        }
    }

    public static class InvalidDate extends ArgumentException {
        private final String invalidDate;

        public InvalidDate(String invalidDate) {
            this.invalidDate = invalidDate;
        }

        @Override
        protected void handle(Sender sender) {
            Message.ILLEGAL_DATE_ERROR.send(sender, this.invalidDate);
        }
    }
}