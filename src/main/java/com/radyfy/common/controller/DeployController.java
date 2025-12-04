package com.radyfy.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@RequestMapping(value = "/api/public")
public class DeployController {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

    @RequestMapping(value = "/deploy-fe", method = RequestMethod.GET)
    public String gitPull() throws IOException, InterruptedException {
        run("cd /opt/codebase/school-erp-fe-admin && git pull", false, true);
        return "Done";
    }

    public void run(String command, boolean wait, boolean sleep) throws IOException, InterruptedException {
        PrintOutput outputMessage = runCommand(command, wait, sleep);
        outputMessage.run();
    }

    private PrintOutput runCommand(String command, boolean wait, boolean sleep)
            throws IOException, InterruptedException {
        Runtime rt = Runtime.getRuntime();
        PrintOutput errorReported, outputMessage;
        String[] commandline = new String[3];
        commandline[0] = "bash";
        commandline[1] = "-c";
        commandline[2] = command;
        Process proc = rt.exec(commandline);
        if (wait)
            proc.waitFor();
        errorReported = getStreamWrapper(proc.getErrorStream(), "ERROR");
        outputMessage = getStreamWrapper(proc.getInputStream(), "OUTPUT");
        if (sleep)
            Thread.sleep(1000);
        errorReported.run();
        return outputMessage;
    }

    public PrintOutput getStreamWrapper(InputStream is, String type) {
        return new PrintOutput(is, type);
    }

    private class PrintOutput {
        InputStream is = null;

        PrintOutput(InputStream is, String type) {
            this.is = is;
        }

        public String get() throws IOException {
            String s = null;
            StringBuilder builder = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            boolean firstLine = true;
            while ((s = br.readLine()) != null) {
                builder.append(s);
                if (firstLine) {
                    firstLine = false;
                } else {
                    builder.append("\n");
                }
            }
            return builder.toString();
        }

        public void run() {
            String s = null;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((s = br.readLine()) != null) {
                    LOG.info(s);
                }
            } catch (IOException ioe) {
                LOG.error(ioe.getMessage(), ioe);
            }
        }
    }
}
