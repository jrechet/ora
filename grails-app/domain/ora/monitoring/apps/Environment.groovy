package ora.monitoring.apps

/**
 * Environment where applications are deployed.
 */
class Environment implements Serializable {
    String logicalName // dev, prod, etc.
    String tenant // customer name
    Integer level

    static hasMany = [applications: ApplicationInstance]

    static constraints = {
        logicalName nullable: false, blank: false
        tenant nullable: false, blank: false
        level nullable: true
    }

    static mapping = {
        table 'environment'
        version false
    }

    String getKey() {
        "${logicalName}::${tenant}"
    }
}