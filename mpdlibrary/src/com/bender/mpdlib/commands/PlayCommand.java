package com.bender.mpdlib.commands;

import com.bender.mpdlib.Pipe;

/**
 * todo: replace with documentation
 */
public class PlayCommand extends BasicCommand
{
    public PlayCommand(Pipe pipe)
    {
        super(pipe, MpdCommands.play);
    }
}
