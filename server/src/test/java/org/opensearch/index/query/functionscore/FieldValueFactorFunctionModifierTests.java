/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.index.query.functionscore;

import org.opensearch.common.io.stream.BytesStreamOutput;
import org.opensearch.common.lucene.search.function.FieldValueFactorFunction;
import org.opensearch.core.common.io.stream.StreamInput;
import org.opensearch.test.OpenSearchTestCase;

import static org.hamcrest.Matchers.equalTo;

public class FieldValueFactorFunctionModifierTests extends OpenSearchTestCase {

    public void testValidOrdinals() {
        assertThat(FieldValueFactorFunction.Modifier.NONE.ordinal(), equalTo(0));
        assertThat(FieldValueFactorFunction.Modifier.LOG.ordinal(), equalTo(1));
        assertThat(FieldValueFactorFunction.Modifier.LOG1P.ordinal(), equalTo(2));
        assertThat(FieldValueFactorFunction.Modifier.LOG2P.ordinal(), equalTo(3));
        assertThat(FieldValueFactorFunction.Modifier.LN.ordinal(), equalTo(4));
        assertThat(FieldValueFactorFunction.Modifier.LN1P.ordinal(), equalTo(5));
        assertThat(FieldValueFactorFunction.Modifier.LN2P.ordinal(), equalTo(6));
        assertThat(FieldValueFactorFunction.Modifier.SQUARE.ordinal(), equalTo(7));
        assertThat(FieldValueFactorFunction.Modifier.SQRT.ordinal(), equalTo(8));
        assertThat(FieldValueFactorFunction.Modifier.RECIPROCAL.ordinal(), equalTo(9));
    }

    public void testWriteTo() throws Exception {
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.NONE.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(0));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.LOG.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(1));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.LOG1P.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(2));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.LOG2P.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(3));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.LN.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(4));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.LN1P.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(5));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.LN2P.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(6));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.SQUARE.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(7));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.SQRT.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(8));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            FieldValueFactorFunction.Modifier.RECIPROCAL.writeTo(out);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(in.readVInt(), equalTo(9));
            }
        }
    }

    public void testReadFrom() throws Exception {
        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(0);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.NONE));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(1);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.LOG));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(2);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.LOG1P));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(3);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.LOG2P));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(4);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.LN));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(5);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.LN1P));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(6);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.LN2P));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(7);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.SQUARE));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(8);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.SQRT));
            }
        }

        try (BytesStreamOutput out = new BytesStreamOutput()) {
            out.writeVInt(9);
            try (StreamInput in = out.bytes().streamInput()) {
                assertThat(FieldValueFactorFunction.Modifier.readFromStream(in), equalTo(FieldValueFactorFunction.Modifier.RECIPROCAL));
            }
        }
    }

    public void testFromString() {
        assertThat(FieldValueFactorFunction.Modifier.fromString("none"), equalTo(FieldValueFactorFunction.Modifier.NONE));
        assertThat(FieldValueFactorFunction.Modifier.fromString("log"), equalTo(FieldValueFactorFunction.Modifier.LOG));
        assertThat(FieldValueFactorFunction.Modifier.fromString("log1p"), equalTo(FieldValueFactorFunction.Modifier.LOG1P));
        assertThat(FieldValueFactorFunction.Modifier.fromString("log2p"), equalTo(FieldValueFactorFunction.Modifier.LOG2P));
        assertThat(FieldValueFactorFunction.Modifier.fromString("ln"), equalTo(FieldValueFactorFunction.Modifier.LN));
        assertThat(FieldValueFactorFunction.Modifier.fromString("ln1p"), equalTo(FieldValueFactorFunction.Modifier.LN1P));
        assertThat(FieldValueFactorFunction.Modifier.fromString("ln2p"), equalTo(FieldValueFactorFunction.Modifier.LN2P));
        assertThat(FieldValueFactorFunction.Modifier.fromString("square"), equalTo(FieldValueFactorFunction.Modifier.SQUARE));
        assertThat(FieldValueFactorFunction.Modifier.fromString("sqrt"), equalTo(FieldValueFactorFunction.Modifier.SQRT));
        assertThat(FieldValueFactorFunction.Modifier.fromString("reciprocal"), equalTo(FieldValueFactorFunction.Modifier.RECIPROCAL));
    }
}
