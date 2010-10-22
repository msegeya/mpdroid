package com.bender.mpdlib.simulator.commands;

import com.bender.mpdlib.commands.Response;

import java.io.PrintWriter;

/**
 * todo: replace with documentation
 */
public class NextSimCommand extends SimCommand
{
    public NextSimCommand(PrintWriter simBufferedWriter)
    {
        super(simBufferedWriter);
    }

    @Override
    public void run() throws Exception
    {
        Playlist.next();
        writer.println(Response.OK);
    }
}
