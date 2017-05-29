package com.hurence.logisland.processor.hbase.io;

import com.hurence.logisland.service.hbase.scan.ResultCell;
import org.apache.commons.codec.binary.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TestJsonQualifierAndValueRowSerializer {

    static final String ROW = "row1";

    static final String FAM1 = "colFam1";
    static final String QUAL1 = "colQual1";
    static final String VAL1 = "val1";
    static final long TS1 = 1111111111;

    static final String FAM2 = "colFam2";
    static final String QUAL2 = "colQual2";
    static final String VAL2 = "val2";
    static final long TS2 = 222222222;

    private final byte[] rowKey = ROW.getBytes(StandardCharsets.UTF_8);
    private ResultCell[] cells;

    @Before
    public void setup() {
        final byte[] cell1Fam = FAM1.getBytes(StandardCharsets.UTF_8);
        final byte[] cell1Qual = QUAL1.getBytes(StandardCharsets.UTF_8);
        final byte[] cell1Val = VAL1.getBytes(StandardCharsets.UTF_8);

        final byte[] cell2Fam = FAM2.getBytes(StandardCharsets.UTF_8);
        final byte[] cell2Qual = QUAL2.getBytes(StandardCharsets.UTF_8);
        final byte[] cell2Val = VAL2.getBytes(StandardCharsets.UTF_8);

        final ResultCell cell1 = getResultCell(cell1Fam, cell1Qual, cell1Val, TS1);
        final ResultCell cell2 = getResultCell(cell2Fam, cell2Qual, cell2Val, TS2);

        cells = new ResultCell[] { cell1, cell2 };
    }

    @Test
    public void testSerializeRegular() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final RowSerializer rowSerializer = new JsonQualifierAndValueRowSerializer(StandardCharsets.UTF_8, StandardCharsets.UTF_8);
        rowSerializer.serialize(rowKey, cells, out);

        final String json = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertEquals("{\"" + QUAL1 + "\":\"" + VAL1 + "\", \"" + QUAL2 + "\":\"" + VAL2 + "\"}", json);
    }

    @Test
    public void testSerializeWithBase64() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final RowSerializer rowSerializer = new JsonQualifierAndValueRowSerializer(StandardCharsets.UTF_8, StandardCharsets.UTF_8, true);
        rowSerializer.serialize(rowKey, cells, out);

        final String qual1Base64 = Base64.encodeBase64String(QUAL1.getBytes(StandardCharsets.UTF_8));
        final String val1Base64 = Base64.encodeBase64String(VAL1.getBytes(StandardCharsets.UTF_8));

        final String qual2Base64 = Base64.encodeBase64String(QUAL2.getBytes(StandardCharsets.UTF_8));
        final String val2Base64 = Base64.encodeBase64String(VAL2.getBytes(StandardCharsets.UTF_8));

        final String json = out.toString(StandardCharsets.UTF_8.name());
        Assert.assertEquals("{\"" + qual1Base64 + "\":\"" + val1Base64 + "\", \"" + qual2Base64 + "\":\"" + val2Base64 + "\"}", json);
    }

    private ResultCell getResultCell(byte[] fam, byte[] qual, byte[] val, long timestamp) {
        final ResultCell cell = new ResultCell();

        cell.setFamilyArray(fam);
        cell.setFamilyOffset(0);
        cell.setFamilyLength((byte)fam.length);

        cell.setQualifierArray(qual);
        cell.setQualifierOffset(0);
        cell.setQualifierLength(qual.length);

        cell.setValueArray(val);
        cell.setValueOffset(0);
        cell.setValueLength(val.length);

        cell.setTimestamp(timestamp);

        return cell;
    }

}