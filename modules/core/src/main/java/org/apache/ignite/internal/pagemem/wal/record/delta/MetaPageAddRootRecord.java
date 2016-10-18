/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.pagemem.wal.record.delta;

import java.nio.ByteBuffer;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.internal.pagemem.PageMemory;
import org.apache.ignite.internal.processors.cache.database.tree.io.BPlusMetaIO;

/**
 * New root in meta page.
 */
public class MetaPageAddRootRecord extends PageDeltaRecord {
    /** */
    private long rootId;

    /**
     * @param cacheId Cache ID.
     * @param pageId  Page ID.
     * @param rootId Root ID.
     */
    public MetaPageAddRootRecord(int cacheId, long pageId, long rootId) {
        super(cacheId, pageId);

        this.rootId = rootId;
    }

    /** {@inheritDoc} */
    @Override public void applyDelta(PageMemory pageMem, ByteBuffer buf) throws IgniteCheckedException {
        BPlusMetaIO io = BPlusMetaIO.VERSIONS.forPage(buf);

        io.addRoot(buf, rootId);
    }

    /** {@inheritDoc} */
    @Override public RecordType type() {
        return RecordType.BTREE_META_PAGE_ADD_ROOT;
    }

    public long rootId() {
        return rootId;
    }
}