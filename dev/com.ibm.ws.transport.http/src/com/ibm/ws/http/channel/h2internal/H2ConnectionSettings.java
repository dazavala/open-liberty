/*******************************************************************************
 * Copyright (c) 1997, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.ws.http.channel.h2internal;

import javax.xml.bind.DatatypeConverter;

import com.ibm.ws.http.channel.h2internal.exceptions.ProtocolException;
import com.ibm.ws.http.channel.h2internal.frames.Frame.FrameDirection;
import com.ibm.ws.http.channel.h2internal.frames.FrameSettings;

/**
 *
 */
public class H2ConnectionSettings {

    // set spec default connection settings
    public int headerTableSize = 4096; // SETTINGS_HEADER_TABLE_SIZE
    public int enablePush = 1; // SETTINGS_ENABLE_PUSH; true (1) by default
    public int maxConcurrentStreams = -1; // SETTINGS_MAX_CONCURRENT_STREAMS: max open push streams, unlimited (-1) by default
    public int initialWindowSize = 65535; // SETTINGS_INITIAL_WINDOW_SIZE
    public int maxFrameSize = 16384; // SETTINGS_MAX_FRAME_SIZE
    public int maxHeaderListSize = -1; // SETTINGS_MAX_HEADER_LIST_SIZE : unlimited (-1) by default

    /**
     * A settings frame can be encoded as a base-64 string and passed as a header on the initial upgrade request.
     * This method decodes that base-64 string and applies the encoded settings to this http2 connection.
     */
    protected void processUpgradeHeaderSettings(String settings) throws ProtocolException {

        if (settings != null) {
            byte[] decoded = DatatypeConverter.parseBase64Binary(settings);
            if (decoded != null) {
                FrameSettings settingsFrame = new FrameSettings(0, decoded.length, (byte) 0x0, false, FrameDirection.READ);
                settingsFrame.processPayload(decoded);
                updateSettings(settingsFrame);
            }
        }
    }

    /**
     * Apply the http2 connection settings contained in a settings frame to this http2 connection
     */
    public void updateSettings(FrameSettings settings) {
        if (settings.getHeaderTableSize() != -1) {
            headerTableSize = settings.getHeaderTableSize();
        }
        if (settings.getEnablePush() != -1) {
            enablePush = settings.getEnablePush();
        }
        if (settings.getMaxConcurrentStreams() != -1) {
            maxConcurrentStreams = settings.getMaxConcurrentStreams();
        }
        if (settings.getInitialWindowSize() != -1) {
            initialWindowSize = settings.getInitialWindowSize();
        }
        if (settings.getMaxFrameSize() != -1) {
            maxFrameSize = settings.getMaxFrameSize();
        }
        if (settings.getMaxHeaderListSize() != -1) {
            this.maxHeaderListSize = settings.getMaxHeaderListSize();
        }
    }

    /**
     * Return the client's enable push setting
     */
    public int getEnablePush() {
        return this.enablePush;
    }

    public int getMaxConcurrentStreams() {
        return this.maxConcurrentStreams;
    }
}
