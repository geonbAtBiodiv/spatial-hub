import geb.spock.GebSpec
import page.SpatialHubHomePage

class ToolTabulateSpec extends GebSpec {

    int pause = 3000

    def setup() {
        when:
        via SpatialHubHomePage

        if (title.startsWith("ALA | Login"))
            authModule.login()

        then:
        waitFor 20, { menuModule.isReady()}
    }

    def "1D tabulate"(){
        when:
        menuModule.clickMenu("Tools ") //NOTICE: space
        menuModule.clickMenuitem("Tabulate - 1D")

        then:
        waitFor 10, { modalModule.title == "Tabulate - 1D" }

        when:
        modalModule.moveToStep(0)

        and:
        //This tool has multiple species selection
        modalModule.speciesTextInput[0].click()
        modalModule.speciesTextInput[0].value("Eucalyptus gunnii")

        then:
        waitFor 10, {modalModule.speciesAutocompleteList.first().text().startsWith("Eucalyptus gunnii")}

        and:
        modalModule.speciesAutocompleteList.first().click()

        //ignore step 3
        when:
        modalModule.moveToStep(1)

        then:
        waitFor 10, {modalModule.availableLayers.size() > 0 }

        when:
        modalModule.selectLayer("Koppen Climate Classification (Major Classes)")


        then:
        waitFor 10, { modalModule.isNextBtnEnabled() }
        modalModule.nextBtn.click()

        waitFor 10, { modalModule.title == "Tabulate - 1D" }
        //waitFor 30, { modalModule.status.contains("running")}  //Could be too quick to capture

        then:
        waitFor 30, { modalModule.reportName == "SpeciesByLayer (species_by_layer.csv)" }
        waitFor 10, { modalModule.reportCSVTable.displayed }

        modalModule.getSizeOfCSVTable() > 2
        modalModule.getCellInCSVTable(0,1) ==  "Eucalyptus gunnii"
        modalModule.getCellInCSVTable(1,1) ==  "Koppen Climate Classification (Major Classes)"

        Float.parseFloat(modalModule.getCellInCSVTable(5,2)) >= 1600

        Thread.sleep(pause)
    }

    /**
     * If no data,  Check if "records.*.csv exist in /data/spatial-data/sample
     * records.*.csv generation is scheduled by Travis
     * @return
     */
    def "2-D tabulation"() {
        when:
        menuModule.clickMenu("Tools ") //NOTICE: space
        menuModule.clickMenuitem("Tabulate - 2D")

        then:
        waitFor 10, { modalModule.title == "Tabulate - 2D" }

        when:
        toolTabulateModule.layer1.click()
        toolTabulateModule.selectLayer1("ASGS Australian States and Territories")

        toolTabulateModule.layer2.click()
        toolTabulateModule.selectLayer2("Koppen Climate Classification (Major Classes)")

        toolTabulateModule.reportType.click()
        toolTabulateModule.selectReportType("area")

        Thread.sleep(1000)

        modalModule.nextBtn.click()

        then:
        waitFor 20, { toolTabulateModule.reportIframe.displayed }

        //Need to switch iFrame before access an element in iFrame
        driver.switchTo().frame("outputDocs")

        //moveToElement(toolTabulateModule.reportTable)  //not working? why
        waitFor 10, { toolTabulateModule.reportDisplayed() }
        toolTabulateModule.getTableSize() > 2
        toolTabulateModule.getCellInReport(1,0) == "New South Wales"
        Float.parseFloat(toolTabulateModule.getCellInReport(1,1)) > 111100

        Thread.sleep(pause)
    }
}