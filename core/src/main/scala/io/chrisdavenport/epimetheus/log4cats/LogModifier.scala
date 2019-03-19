package io.chrisdavenport.epimetheus
package log4cats

import cats._
import cats.implicits._
import cats.effect._
import io.chrisdavenport.log4cats._
import io.chrisdavenport.log4cats.extras.LogLevel
import io.chrisdavenport.log4cats.extras.LogLevel._
import shapeless._

sealed abstract class LogModifier[F[_]]{
  def selfAware(s: SelfAwareLogger[F]): SelfAwareLogger[F]
  def selfAwareStructured(s: SelfAwareStructuredLogger[F]): SelfAwareStructuredLogger[F]
}


object LogModifier {

  /**
   * Register a LogTransformer with the CollectorRegistry that can
   * modify loggers such that their logging will be reflected
   * in the registry.
   */
  def register[F[_]: Sync](cr: CollectorRegistry[F], name: Name = Name("log4cats_total")): F[LogModifier[F]] = {
    for {
      counter <- Counter.labelled(
        cr, 
        name,
        "Log4cats Log Totals.",
        Sized(Label("level")),
        {l: LogLevel => Sized(reportLevel(l))}
      )
    } yield new MeteredLogTransformer[F](counter)
  }

  /**
   * Convenience Constructor for when you only want to
   * modify a single SelfAwareLogger
   */
  def selfAware[F[_]: Sync](
    cr: CollectorRegistry[F],
    selfAware: SelfAwareLogger[F],
    name: Name = Name("log4cats_total")
  ): F[SelfAwareLogger[F]] = 
    register(cr, name).map(_.selfAware(selfAware))

  /**
   * Convenience Constructor for when you only want to
   * modify a single SelfAwareStructuredLogger
   */
  def selfAwareStructured[F[_]: Sync](
    cr: CollectorRegistry[F],
    selfAware: SelfAwareStructuredLogger[F],
    name: Name = Name("log4cats_total")
  ): F[SelfAwareStructuredLogger[F]] = 
    register(cr, name).map(_.selfAwareStructured(selfAware))

  private def reportLevel(l: LogLevel): String = l match {
    case Error => "error"
    case Warn => "warn"
    case Info => "info"
    case Debug => "debug"
    case Trace => "trace"
  }

  private class MeteredLogTransformer[F[_]](
    val c: UnlabelledCounter[F, LogLevel]
  )(implicit F: Monad[F]) extends LogModifier[F]{
    def selfAware(s: SelfAwareLogger[F]): SelfAwareLogger[F] = 
      new MeteredSelfAwareLogger[F](s, c)
    def selfAwareStructured(s: SelfAwareStructuredLogger[F]): SelfAwareStructuredLogger[F] =
      new MeteredSelfAwareStructureLogger[F](s, c)
  }
  
  private class MeteredSelfAwareLogger[F[_]](
    val l: SelfAwareLogger[F],
    val c: UnlabelledCounter[F, LogLevel]
  )(implicit F: Monad[F]) extends SelfAwareLogger[F]{
    // Members declared in io.chrisdavenport.log4cats.ErrorLogger
    def debug(t: Throwable)(message: => String): F[Unit] =
      l.debug(t)(message) >>
        isDebugEnabled.ifM(c.label(Debug).inc, F.unit)
    def error(t: Throwable)(message: => String): F[Unit] =
      l.error(t)(message) >>
        isErrorEnabled.ifM(c.label(Error).inc, F.unit)
    def info(t: Throwable)(message: => String): F[Unit] =
      l.info(t)(message) >>
        isInfoEnabled.ifM(c.label(Info).inc, F.unit) 
    def trace(t: Throwable)(message: => String): F[Unit] = 
      l.trace(t)(message) >>
        isTraceEnabled.ifM(c.label(Trace).inc, F.unit)
    def warn(t: Throwable)(message: => String): F[Unit] =
      l.warn(t)(message) >>
        isWarnEnabled.ifM(c.label(Warn).inc, F.unit)
    
    // Members declared in io.chrisdavenport.log4cats.MessageLogger
    def debug(message: => String): F[Unit] = 
      l.debug(message) >>
        isDebugEnabled.ifM(c.label(Debug).inc, F.unit)
    def error(message: => String): F[Unit] = 
      l.error(message) >>
        isDebugEnabled.ifM(c.label(Error).inc, F.unit)
    def info(message: => String): F[Unit] = 
      l.info(message) >>
        isInfoEnabled.ifM(c.label(Info).inc, F.unit)
    def trace(message: => String): F[Unit] = 
      l.trace(message) >>
        isTraceEnabled.ifM(c.label(Trace).inc, F.unit)
    def warn(message: => String): F[Unit] = 
      l.warn(message) >>
        isWarnEnabled.ifM(c.label(Warn).inc, F.unit)
    
    // Members declared in io.chrisdavenport.log4cats.SelfAwareLogger
    def isDebugEnabled: F[Boolean] = l.isDebugEnabled
    def isErrorEnabled: F[Boolean] = l.isErrorEnabled
    def isInfoEnabled: F[Boolean] = l.isInfoEnabled
    def isTraceEnabled: F[Boolean] = l.isTraceEnabled
    def isWarnEnabled: F[Boolean] = l.isWarnEnabled

  }

  private class MeteredSelfAwareStructureLogger[F[_]](
    val l: SelfAwareStructuredLogger[F],
    val c: UnlabelledCounter[F, LogLevel]
  )(implicit F: Monad[F]) extends SelfAwareStructuredLogger[F]{
        // Members declared in io.chrisdavenport.log4cats.ErrorLogger
    def debug(t: Throwable)(message: => String): F[Unit] =
      l.debug(t)(message) >>
        isDebugEnabled.ifM(c.label(Debug).inc, F.unit)
    def error(t: Throwable)(message: => String): F[Unit] =
      l.error(t)(message) >>
        isErrorEnabled.ifM(c.label(Error).inc, F.unit)
    def info(t: Throwable)(message: => String): F[Unit] =
      l.info(t)(message) >>
        isInfoEnabled.ifM(c.label(Info).inc, F.unit) 
    def trace(t: Throwable)(message: => String): F[Unit] = 
      l.trace(t)(message) >>
        isTraceEnabled.ifM(c.label(Trace).inc, F.unit)
    def warn(t: Throwable)(message: => String): F[Unit] =
      l.warn(t)(message) >>
        isWarnEnabled.ifM(c.label(Warn).inc, F.unit)
    
    // Members declared in io.chrisdavenport.log4cats.MessageLogger
    def debug(message: => String): F[Unit] = 
      l.debug(message) >>
        isDebugEnabled.ifM(c.label(Debug).inc, F.unit)
    def error(message: => String): F[Unit] = 
      l.error(message) >>
        isDebugEnabled.ifM(c.label(Error).inc, F.unit)
    def info(message: => String): F[Unit] = 
      l.info(message) >>
        isInfoEnabled.ifM(c.label(Info).inc, F.unit)
    def trace(message: => String): F[Unit] = 
      l.trace(message) >>
        isTraceEnabled.ifM(c.label(Trace).inc, F.unit)
    def warn(message: => String): F[Unit] = 
      l.warn(message) >>
        isWarnEnabled.ifM(c.label(Warn).inc, F.unit)
    
    // Members declared in io.chrisdavenport.log4cats.SelfAwareLogger
    def isDebugEnabled: F[Boolean] = l.isDebugEnabled
    def isErrorEnabled: F[Boolean] = l.isErrorEnabled
    def isInfoEnabled: F[Boolean] = l.isInfoEnabled
    def isTraceEnabled: F[Boolean] = l.isTraceEnabled
    def isWarnEnabled: F[Boolean] = l.isWarnEnabled

    /** As seen from class MeteredSelfAwareStructureLogger, the missing signatures are as follows.
     *  For convenience, these are usable as stub implementations.
     */
    def debug(ctx: Map[String,String],t: Throwable)(msg: => String): F[Unit] = 
      l.debug(ctx, t)(msg) >>
        isDebugEnabled.ifM(c.label(Debug).inc, F.unit)
    def debug(ctx: Map[String,String])(msg: => String): F[Unit] = 
      l.debug(ctx)(msg) >>
        isDebugEnabled.ifM(c.label(Debug).inc, F.unit)
    def error(ctx: Map[String,String],t: Throwable)(msg: => String): F[Unit] = 
      l.error(ctx, t)(msg) >>
        isErrorEnabled.ifM(c.label(Error).inc, F.unit)
    def error(ctx: Map[String,String])(msg: => String): F[Unit] = 
      l.error(ctx)(msg) >>
        isErrorEnabled.ifM(c.label(Error).inc, F.unit)
    def info(ctx: Map[String,String],t: Throwable)(msg: => String): F[Unit] = 
      l.info(ctx, t)(msg) >>
        isInfoEnabled.ifM(c.label(Info).inc, F.unit)
    def info(ctx: Map[String,String])(msg: => String): F[Unit] = 
      l.info(ctx)(msg) >>
        isInfoEnabled.ifM(c.label(Info).inc, F.unit)
    def trace(ctx: Map[String,String],t: Throwable)(msg: => String): F[Unit] = 
      l.trace(ctx, t)(msg) >>
        isTraceEnabled.ifM(c.label(Trace).inc, F.unit)
    def trace(ctx: Map[String,String])(msg: => String): F[Unit] = 
      l.trace(ctx)(msg) >>
        isTraceEnabled.ifM(c.label(Trace).inc, F.unit)
    def warn(ctx: Map[String,String],t: Throwable)(msg: => String): F[Unit] = 
      l.warn(ctx)(msg) >>
        isWarnEnabled.ifM(c.label(Trace).inc, F.unit)
    def warn(ctx: Map[String,String])(msg: => String): F[Unit] = 
      l.warn(ctx)(msg) >>
        isWarnEnabled.ifM(c.label(Trace).inc, F.unit)

  }
}