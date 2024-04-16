package com.sourcemuse.gradle.plugin

import com.mongodb.ServerAddress

import java.util.regex.Pattern

import static com.sourcemuse.gradle.plugin.LogDestination.FILE
import static java.lang.Integer.parseInt

class GradleMongoPluginExtension {

    static final EPHEMERAL_TEMPORARY_FOLDER = null
    static final Pattern VALID_MONGOD_VERBOSITY_FORMAT = ~/(?i)-?v+|-{0,2}verbose/

    private int port = ServerAddress.defaultPort()
    String bindIp = ServerAddress.defaultHost()
    boolean journalingEnabled = false
    String logging = FILE as String
    String logFilePath = 'embedded-mongo.log'
    String mongoVersion = 'PRODUCTION'
    String storageEngine = 'wiredTiger'
    String storageLocation = EPHEMERAL_TEMPORARY_FOLDER
    String mongodVerbosity = ''
    String downloadUrl = ''
    String proxyHost = ''
    int proxyPort = 80
    String artifactStorePath = ''
    boolean auth = false
    Map<String, String> args = [:]
    Map<String, String> params = [:]
    Integer syncDelay = null
    String replicaSet = null

    void setDownloadUrl(String url) {
        try {
            this.downloadUrl = new URL(url).toString()
        } catch (MalformedURLException ignored) {
            throw new IllegalArgumentException("DownloadURL ${url} is not a valid URL.")
        }
    }

    void setDownloadURL(String url) {
        println "Warning: 'downloadURL' has been deprecated. It has been renamed to 'downloadUrl' " +
            "and will no longer be supported in a future release."
        setDownloadUrl(url)
    }

    int getPort() {
        port
    }

    void setPort(Object port) {
        if (port instanceof String) {
            this.port = parsePortAsString(port)
        } else {
            this.port = port as Integer
        }
    }

    void setMongodVerbosity(String mongodVerbosity) {
        this.mongodVerbosity = parseMongodVerbosity(mongodVerbosity)
    }

    private static Serializable parseMongodVerbosity(String mongodVerbosity) {
        if (!(mongodVerbosity ==~ VALID_MONGOD_VERBOSITY_FORMAT))
            throw new IllegalArgumentException("MongodVerbosity should be defined as either '-verbose' or '-v(vvvv)'. " +
                "Do not configure this property if you don't wish to have verbose output.")

        def lowerCaseValue = mongodVerbosity.toLowerCase()

        if (lowerCaseValue.endsWith('verbose')) return '-v'
        if (lowerCaseValue.startsWith('v')) return "-$lowerCaseValue"
        return lowerCaseValue
    }

    private static int parsePortAsString(String port) {
        if (port.toLowerCase() == 'random') {
            return randomAvailablePort()
        }

        return parseInt(port)
    }

    private static int randomAvailablePort() {
        try {
            ServerSocket server = new ServerSocket()
            server.setReuseAddress(true)
            server.bind(new InetSocketAddress(0))
            int port = server.getLocalPort()
            server.close()
            return port
        } catch (IOException e) {
            throw new IOException('Failed to find random free port', e)
        }
    }

    GradleMongoPluginExtension overrideWith(GradleMongoPluginExtension pluginExtensionOverride) {
        def mergedPluginExtension = new GradleMongoPluginExtension()
        applyNonDefaultProperties(this, mergedPluginExtension)
        applyNonDefaultProperties(pluginExtensionOverride, mergedPluginExtension)
        mergedPluginExtension
    }

    private
    static void applyNonDefaultProperties(GradleMongoPluginExtension sourcePluginExtension, GradleMongoPluginExtension targetPluginExtension) {
        def pluginExtensionWithDefaults = new GradleMongoPluginExtension()
        pluginExtensionWithDefaults.properties.findAll { it.key != 'class' }.each { String key, Object value ->
            if (sourcePluginExtension[key] != value) {
                targetPluginExtension[key] = sourcePluginExtension[key]
            }
        }
    }
}
