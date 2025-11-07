String buildLog = new File("${basedir}/build.log").getText("UTF-8")

assert buildLog.contains("--profile dev" as CharSequence)
assert buildLog.contains("--profile debug" as CharSequence)
assert buildLog.contains("Creating mpdc-it-up-down-profiles-base" as CharSequence)
assert buildLog.contains("Creating mpdc-it-up-down-profiles-dev" as CharSequence)
assert buildLog.contains("Creating mpdc-it-up-down-profiles-debug" as CharSequence)
assert !buildLog.contains("Creating mpdc-it-up-down-profiles-prod" as CharSequence)
