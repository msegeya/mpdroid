package com.bender.mpdlib.commands;

import com.bender.mpdlib.MpdStatus;
import com.bender.mpdlib.Pipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class GetStatusCommand extends Command<NullArg, Result<List<StatusTuple>>>
{
    public GetStatusCommand(Pipe pipe)
    {
        super(pipe);
    }

    @Override
    protected void executeCommand(NullArg arg) throws IOException
    {
        pipe.write(MpdCommands.status.toString());
    }

    @Override
    protected Result<List<StatusTuple>> readResult() throws IOException
    {
        List<StatusTuple> statuses = new ArrayList<StatusTuple>();
        String line;
        while (!isLastLine(line = pipe.readLine()))
        {
            StatusTuple statusTuple = MpdStatus.parse(line);
            statuses.add(statusTuple);
        }
        Status status = Status.parse(line);
        Result<List<StatusTuple>> listResult = new Result<List<StatusTuple>>();
        listResult.status = status;
        listResult.result = statuses;
        return listResult;
    }

    private boolean isLastLine(String line)
    {
        return Response.isResponseLine(line);
    }
}
