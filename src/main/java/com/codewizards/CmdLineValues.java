package com.codewizards;

import lombok.Getter;
import org.kohsuke.args4j.Option;

public class CmdLineValues {

    @Getter
    @Option(required = true, name = "--server-id", usage = "Name of the server")
    private String serverId;

    @Getter
    @Option(required = true, name = "--config-path", usage = "Path to servers configuration file")
    private String serversConf;

}
