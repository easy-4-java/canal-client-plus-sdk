package com.alibaba.otter.canal.util;

import com.alibaba.otter.canal.client.impl.ClusterNodeAccessStrategy;
import com.alibaba.otter.canal.common.zookeeper.ZkClientx;
import com.alibaba.otter.canal.common.zookeeper.ZookeeperPathUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LangMigrationTest {
    @Test
    void lang3PreservesNullAndMixedCaseStringBehavior() {
        for (String value : Arrays.asList(null, "", "blob", "LongBLoB", "varBINARY", "not-a-type")) {
            assertEquals(org.apache.commons.lang.StringUtils.isNotEmpty(value),
                    org.apache.commons.lang3.StringUtils.isNotEmpty(value));
            for (String token : Arrays.asList(null, "", "BLOB", "BINARY", "XA_TYPE", "XA_XID")) {
                assertEquals(org.apache.commons.lang.StringUtils.containsIgnoreCase(value, token),
                        org.apache.commons.lang3.StringUtils.containsIgnoreCase(value, token));
                assertEquals(org.apache.commons.lang.StringUtils.endsWithIgnoreCase(value, token),
                        org.apache.commons.lang3.StringUtils.endsWithIgnoreCase(value, token));
            }
        }
        assertDoesNotThrow(() -> CanalUtils.printXAInfo(null));
        assertDoesNotThrow(() -> CanalUtils.printXAInfo(Arrays.asList(
                CanalEntry.Pair.newBuilder().setKey("prefix_xa_type").setValue("COMMIT").build(),
                CanalEntry.Pair.newBuilder().setKey("prefix_Xa_XiD").setValue("id").build())));
        for (String type : Arrays.asList("", "blob", "LongBLoB", "varBINARY", "varchar")) {
            assertDoesNotThrow(() -> CanalUtils.printColumn(Collections.singletonList(
                    CanalEntry.Column.newBuilder().setMysqlType(type).setName("payload")
                            .setValue("value").build())));
        }
    }

    @Test
    void loggingFormatsKeepPlatformNewlines() {
        assertEquals(System.lineSeparator(), CanalUtils.SEP);
        assertTrue(CanalUtils.context_format.startsWith(System.lineSeparator()));
        assertTrue(CanalUtils.context_format.endsWith(System.lineSeparator()));
        assertTrue(CanalUtils.row_format.endsWith(System.lineSeparator()));
        assertTrue(CanalUtils.transaction_format.endsWith(System.lineSeparator()));
    }

    @Test
    void sdkBytecodeDoesNotReferenceLegacyLangPackage() throws Exception {
        for (Class<?> type : Arrays.asList(CanalUtils.class,
                com.alibaba.otter.canal.client.AbstractClientBuilder.class,
                com.alibaba.otter.canal.client.AbstractCanalClient.class)) {
            String path = "/" + type.getName().replace('.', '/') + ".class";
            try (InputStream input = type.getResourceAsStream(path)) {
                assertNotNull(input);
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) bytes.write(buffer, 0, count);
                assertFalse(new String(bytes.toByteArray(), StandardCharsets.ISO_8859_1)
                        .contains("org/apache/commons/lang/"));
            }
        }
    }

    @Test
    void canalClusterPathStillResolvesLegacyRuntimeWithoutNetwork() {
        ZkClientx zk = mock(ZkClientx.class);
        String cluster = ZookeeperPathUtils.getDestinationClusterRoot("example");
        when(zk.getChildren(cluster)).thenReturn(Collections.singletonList("127.0.0.1:11111"));
        ClusterNodeAccessStrategy strategy = new ClusterNodeAccessStrategy("example", zk);
        assertEquals(new InetSocketAddress("127.0.0.1", 11111), strategy.currentNode());
        verify(zk).getChildren(cluster);
        assertNotNull(org.apache.commons.lang.StringUtils.class.getProtectionDomain().getCodeSource());
        assertNotNull(org.apache.commons.lang3.StringUtils.class.getProtectionDomain().getCodeSource());
    }
}
