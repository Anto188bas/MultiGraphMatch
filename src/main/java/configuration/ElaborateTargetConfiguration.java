package configuration;

public class ElaborateTargetConfiguration {
    public String nodesDirectory;
    public String edgesDirectory;
    public String outDirectory;

    public ElaborateTargetConfiguration(String[] args) {
        for (int i = 0; i < args.length; i++)
            readParameter(args[i], args[++i]);
        validateParameters();
    }

    public void readParameter(String key, String value) {
        switch (key) {
            case "-n" -> nodesDirectory = value;
            case "-e" -> edgesDirectory = value;
            case "-o" -> outDirectory = value;
            default -> printHelp(key);
        }
    }

    private void printHelp(String parameter) {
        String help = "Usage: java -jar ElaborateTarget.jar -n <nodesFolder> -e <edgesFolder> -o <outDirectory>\n\n";
        help += "REQUIRED PARAMETERS:\n";
        help += "-n\tTarget nodes directory\n";
        help += "-e\tTarget edges directory\n";
        help += "-o\tOut directory\n\n";
        if (parameter != null) System.out.println("Error! Unrecognizable command '" + parameter + "'");
        System.out.println(help);
    }

    private void printError(String parameter) {
        if (parameter != null) return;
        System.out.println("Error!");
        printHelp(null);
        System.exit(1);
    }

    private void validateParameters() {
        printError(nodesDirectory);
        printError(edgesDirectory);
        printError(outDirectory);
    }
}
