package clayborn.universalremote.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

import clayborn.universalremote.settings.Strings;

public class Logger {

	private org.apache.logging.log4j.Logger m_logSink;
	
	public Logger(org.apache.logging.log4j.Logger logger)
	{
		m_logSink = logger;
	}
	
	public void logException (String s, Throwable e)
	{
		this.error(s);
		m_logSink.error(e.getMessage());
		m_logSink.error(ExceptionUtils.getStackTrace(e));
	}
	
	public void error (String s, Object... arguments)
	{
		m_logSink.error(Strings.LOGPREFIXZ + s, arguments);
	}

	public void warn (String s, Object... arguments)
	{
		m_logSink.warn(Strings.LOGPREFIXZ + s,arguments);
	}
	
	public void info (String s, Object... arguments)
	{
		m_logSink.info(Strings.LOGPREFIXZ + s,arguments);
	}

	
}
