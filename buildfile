# Version number for this release
VERSION_NUMBER = "1.0.0"
# Group identifier for your projects
GROUP = "com.freiheit.fuava"
COPYRIGHT = "freiheit.com technologies GmbH (2010)"

# Specify Maven 2.0 remote repositories here, like this:
#repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << "http://repo1.maven.org/maven2/"
repositories.release_to[:url] = 'http://intranet.toxine.lan:8081/artifactory/libs-releases'

DEP_LOGGING='org.slf4j:slf4j-api:jar:1.6.1'

desc "The Freiheit Fuava Call-Tree-Profiler project"
define "fuava-ctprofiler" do

  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
  manifest['Bundle-License'] = "http://www.apache.org/licenses/LICENSE-2.0"
  compile.options.target = '1.5'
  compile.options.source = '1.5'

  package_with_sources
  package_with_javadoc

  desc "Call Tree Profiling - core library"
  define "core" do
    compile.with # Add classpath dependencies
    package(:jar)
  end

  desc "Call Tree Profiling - Java Servlet extensions"
  define "servlet" do
    compile.with 'org.mortbay.jetty:servlet-api:jar:2.5-6.0.1', DEP_LOGGING, project("core")
    package(:jar)
  end

  desc "Call Tree Profiling - Automatic profiling using AOP"
  define "aop" do
    compile.with 'aopalliance:aopalliance:jar:1.0', project("core")
    package(:jar)
  end

  desc "Call Tree Profiling - Spring integration"
  define "spring" do
    compile.with 'org.springframework:spring-core:jar:3.0.1.RELEASE','org.springframework:spring-beans:jar:3.0.1.RELEASE', project("core")
    package(:jar)
  end

  desc "Call Tree Profiling - Guice integration"
  define "guice" do
    compile.with transitive('com.google.inject:guice:jar:3.0', project('core'))
    package(:jar)
  end

end
