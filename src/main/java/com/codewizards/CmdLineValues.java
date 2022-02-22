package com.codewizards;

import lombok.Getter;
import org.kohsuke.args4j.Option;

public class CmdLineValues {

    @Getter
    @Option(required = true, name = "-i", usage = "Name of the server")
    private String serverId;

    @Getter
    @Option(required = true, name = "-f", usage = "Path to servers configuration file")
    private String serversConf;

}
