package boris.company.voicemovie.command;

public record TextSearchCommand(String text) implements MovieSearchCommand {
}
