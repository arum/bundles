
<ivy:settings file="../uk.co.arum.osgi.build/ivysettings.xml" />

<property file="${user.home}/build.properties" />

<property file="../uk.co.arum.osgi.build/libs/libs.properties" prefix="lib" />
<property file="../uk.co.arum.osgi.build/classpath.properties" />

<path id="compile.classpath">
	<fileset dir="${lib.osgi}">
		<include name="**/*.jar" />
	</fileset>
	<pathelement id="addition.classpath" />
</path>


<!-- Called by the master Ant script. -->
<target name="complete" depends="cleanbuild, cleancompile, with.additional.compile, without.additional.compile, build" />

<!-- Intended to build called from the IDE -->
<target name="default" depends="cleanbuild, build" />

<target name="with.additional.compile" if="has.additional.compile">
	<antcall target="additional.compile" />
	<antcall target="compile" />
</target>

<target name="without.additional.compile" unless="has.additional.compile">
	<antcall target="compile" />
</target>

<target name="cleancompile">
	<delete dir="bin" />
	<mkdir dir="bin" />
</target>

<target name="cleanbuild">
	<delete dir="dist" />
	<mkdir dir="dist" />
</target>

<target name="compile">
	<ivy:retrieve />

	<ivy:cachepath pathid="classpath.compile" />

	<taskdef name="osgijc" classname="uk.org.brindy.osgijc.OSGiJavac">
		<classpath>
			<path refid="classpath.compile" />
		</classpath>
	</taskdef>

	<pathconvert property="flat.path" pathsep=",">
		<path refid="classpath.compile" />
	</pathconvert>

	<echo>[${flat.path}]</echo>

	<osgijc bundledir="${basedir}">
		<classpath refid="classpath.compile" />
	</osgijc>
</target>

<target name="build" if="module.version">
	<ivy:retrieve />

	<ivy:cachepath pathid="classpath.compile" />

	<taskdef name="osgibb" classname="uk.org.brindy.osgibb.OSGiBundleBuilder">
		<classpath>
			<path refid="classpath.compile" />
		</classpath>
	</taskdef>

	<osgibb basedir="${basedir}" destdir="${basedir}/dist" versionprop="bundle.version" />
	<fail unless="module.version">module.version not specified.</fail>

	<ivy:resolve />
	<ivy:publish resolver="local" artifactspattern="${basedir}/dist/[artifact].[ext]" pubrevision="${module.version}" overwrite="true" />
</target>
