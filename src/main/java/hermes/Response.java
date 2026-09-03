package hermes;

/**
 * What Hermes has to say in reply to one line of input.
 *
 * <p>The reply on its own is not enough for a caller that must close itself
 * once the conversation is over. Letting such a caller recognise the parting
 * word for itself would put a second copy of the command vocabulary outside
 * the parser, so the decision is carried back with the text instead.
 *
 * @param text what to show the user
 * @param isExit whether Hermes should stop after this reply
 */
public record Response(String text, boolean isExit) {
}
