package com.mucommander.commons.file.impl.hadoop;

import com.mucommander.commons.file.AuthenticationType;
import com.mucommander.commons.file.Credentials;
import com.mucommander.commons.file.FileURLTestCase;

/**
 * A {@link FileURLTestCase} implementation for Hadoop HDFS URLs.
 *
 * @author Maxence Bernard
 */
public class HDFSFileURLTest extends FileURLTestCase {

    @Override
    protected String getScheme() {
        return "hdfs";
    }

    @Override
    protected int getDefaultPort() {
        return 8020;
    }

    @Override
    protected AuthenticationType getAuthenticationType() {
        return AuthenticationType.AUTHENTICATION_OPTIONAL;
    }

    @Override
    protected Credentials getGuestCredentials() {
        return null;
    }

    @Override
    protected String getPathSeparator() {
        return "/";
    }

    @Override
    protected boolean isQueryParsed() {
        return true;
    }
}
