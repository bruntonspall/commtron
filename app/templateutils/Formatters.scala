package templateutils

object Formatters {
  def dateFormat(date: org.joda.time.DateTime) = date.toString("HH:mm 'on' dd MMMM, YYYY")
}
