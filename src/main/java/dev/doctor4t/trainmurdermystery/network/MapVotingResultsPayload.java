package dev.doctor4t.trainmurdermystery.network;

import dev.doctor4t.trainmurdermystery.TMM;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashMap;
import java.util.Map;

public class MapVotingResultsPayload implements CustomPacketPayload {
    public static final PacketType<MapVotingResultsPayload> TYPE = PacketType.create(
            () -> TMM.id("map_voting_results"),
            MapVotingResultsPayload::new
    );
    
    public static final StreamCodec<FriendlyByteBuf, MapVotingResultsPayload> CODEC = StreamCodec.composite(
            // 赢得投票的地图ID
            StreamCodec.STRING,
            MapVotingResultsPayload::getWinningMapId,
            // 所有地图的投票数
            StreamCodec.of(MapVotingResultsPayload::encodeVotes, MapVotingResultsPayload::decodeVotes),
            MapVotingResultsPayload::getAllVotes,
            // 投票结束原因
            StreamCodec.STRING,
            MapVotingResultsPayload::getReason,
            MapVotingResultsPayload::new
    );

    private final String winningMapId;
    private final Map<String, Integer> allVotes;
    private final String reason;

    public MapVotingResultsPayload(String winningMapId, Map<String, Integer> allVotes, String reason) {
        this.winningMapId = winningMapId;
        this.allVotes = allVotes != null ? new HashMap<>(allVotes) : new HashMap<>();
        this.reason = reason;
    }

    // 用于解码的构造函数
    public MapVotingResultsPayload(FriendlyByteBuf buf) {
        this.winningMapId = buf.readUtf();
        this.allVotes = decodeVotes(buf);
        this.reason = buf.readUtf();
    }

    // 编码投票数据
    private static Map<String, Integer> decodeVotes(FriendlyByteBuf buf) {
        int size = buf.readInt();
        Map<String, Integer> votes = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String mapId = buf.readUtf();
            int voteCount = buf.readInt();
            votes.put(mapId, voteCount);
        }
        return votes;
    }

    // 解码投票数据
    private static void encodeVotes(FriendlyByteBuf buf, Map<String, Integer> votes) {
        buf.writeInt(votes.size());
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            buf.writeUtf(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    public String getWinningMapId() {
        return winningMapId;
    }

    public Map<String, Integer> getAllVotes() {
        return allVotes;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public PacketType<MapVotingResultsPayload> type() {
        return TYPE;
    }
}