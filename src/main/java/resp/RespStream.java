package resp;

import java.util.concurrent.ConcurrentSkipListMap;

public class RespStream implements RespObject {
    public ConcurrentSkipListMap<RespStreamId, RespStreamEntry> streams;

    public RespStream(ConcurrentSkipListMap<RespStreamId, RespStreamEntry> streams){
        this.streams = streams;
    }

    @Override
    public String getType() {
        return "stream";
    }
}
