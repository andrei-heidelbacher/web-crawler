//val Pattern = """user-agent: \*$((allow|disallow): .*$)*""".r
//val String = "user-agent: *\nallow: /problema/tri3\ndisallow: " +
//  "/problema/adunare"

//val Pattern = """user-agent: \*(\n(allow|disallow): .*)*$""".r
//val String = "user-agent: *\ndisallow: /problema/tri3\nalllow: " +
//  "/problema/adunare"
val Pattern = """.*(?:\n(allow|disallow): (.*))*.*""".r
val String = "\nallow: /problema/adunare\nallow: /problema/tri"
Pattern.unapplySeq(String)
String match {
  case Pattern(_*) =>
    "Matched!"
  case _ =>
    "Not matched!"
}

val permissionRegex = {
  val allow = "(?:a|A)(?:l|L)(?:l|L)(?:o|O)(?:w|W)"
  val disallow = "(?:d|D)(?:i|I)(?:s|S)(?:a|A)(?:l|L)(?:l|L)(?:o|O)(?:w|W)"
  val permissionType = "(" + allow + "|" + disallow + ")"
  val path = "(.*)"
  ("(?:\\n" + permissionType + ": " + path + ")*").r
}