class Job {
    String name
    Settings settings, defaultSettings, branchSettings, trunkSettings
    Logger logger
    Branch branch
    Repository repository

    Job(Branch branch, Settings defaultSettings, Integer settingsIndex = 0) {
        this.branch = branch
        this.logger = branch.logger
        this.repository = branch.repository
        this.defaultSettings = defaultSettings
        //logger.debug("Default settings: ${defaultSettings}")
        this.branchSettings = branch.settings.get(settingsIndex)
        //logger.debug("Branch settings: ${branchSettings}")
        this.trunkSettings = repository.settings
        //logger.debug("Trunk settings: ${trunkSettings}")
        switch (branchSettings.status) {
            case Settings.Status.GOOD:
                settings = branchSettings
                break
            case Settings.Status.NONE:
                logger.debug("The ${branch.name} branch of ${repository.url} does not contain a jenkins.yml.")
                switch (trunkSettings.status) {
                    case Settings.Status.GOOD:
                        logger.debug("Defaulting to trunk settings.")
                        settings = trunkSettings
                        logger.debug("${settings}")
                        break
                    case Settings.Status.NONE:
                        logger.debug("The trunk of ${repository.url} does not contain a jenkins.yml.")
                        logger.debug("Defaulting to default settings.")
                        settings = defaultSettings
                        logger.debug("${settings}")
                        break
                    case Settings.Status.ERROR:
                        settings = trunkSettings
                        break
                }
                break
            case Settings.Status.ERROR:
                settings = branchSettings
                break
        }

        if (branch.settings.size() > 1){
            logger.debug("Entered with multiple branch settings")
            makeName(settingsIndex)
        } else {
            makeName()
        }
    }

    def makeName(settingsIndex = null) {
        if (settings.status == Settings.Status.ERROR) {
            this.name = "auto_${repository.name}_${branch.name.replace("#", "_no_")}_"
        } else {
            if (settingsIndex) {
                this.name = "auto_${repository.name}_${branch.name.replace("#", "_no_")}_${settings.settings.ros.release}_${settingsIndex}"
            } else {
                this.name = "auto_${repository.name}_${branch.name.replace("#", "_no_")}_${settings.settings.ros.release}"
            }
        }
    }

    Job(PullRequest pullRequest, Settings defaultSettings) {
        this(pullRequest.branch, defaultSettings)
    }

    String toString() {
        return name
    }
}
