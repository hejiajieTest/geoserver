/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
 * (c) 2001 - 2013 OpenPlans
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.wps.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.geoserver.web.GeoServerApplication;
import org.geoserver.wps.ppio.ComplexPPIO;
import org.geoserver.wps.ppio.ProcessParameterIO;
import org.geoserver.wps.process.GeoServerProcessors;
import org.geotools.data.Parameter;
import org.geotools.process.ProcessFactory;
import org.opengis.feature.type.Name;

/**
 * A single output parameter, along with the chosen output mime type and the output inclusion flag
 * 
 * @author Andrea Aime - OpenGeo
 */
@SuppressWarnings("serial")
class OutputParameter implements Serializable {
    Name processName;

    String paramName;

    String mimeType;

    boolean include = true;

    public OutputParameter(Name processName, String paramName) {
        this.processName = processName;
        this.paramName = paramName;
        Parameter<?> p = getParameter();
        this.mimeType = getDefaultMime();
    }

    //add start 2020/09/07
    public OutputParameter(){

    };
   
    public OutputParameter(Name processName, String paramName,String mineType) {
        this.processName = processName;
        this.paramName = paramName;
        Parameter<?> p = getParameter();
        this.mimeType = mineType;
    }
    //add end 2020/09/07
    String getDefaultMime() {
        if (!isComplex()) {
            return null;
        } else {
            return ((ComplexPPIO) getProcessParameterIO().get(0)).getMimeType();
        }
    }

    public List<String> getSupportedMime() {
        List<String> results = new ArrayList<String>();
        for (ProcessParameterIO ppio : getProcessParameterIO()) {
            ComplexPPIO cp = (ComplexPPIO) ppio;
            results.add(cp.getMimeType());
        }
        return results;
    }

    public boolean isComplex() {
        List<ProcessParameterIO> ppios = getProcessParameterIO();
        return ppios.size() > 0 && ppios.get(0) instanceof ComplexPPIO;
    }

    List<ProcessParameterIO> getProcessParameterIO() {
        return ProcessParameterIO.findAll(getParameter(), GeoServerApplication.get()
                .getApplicationContext());
    }

    ProcessFactory getProcessFactory() {
        return GeoServerProcessors.createProcessFactory(processName, false);
    }

    Parameter<?> getParameter() {
        return getProcessFactory().getResultInfo(processName, null).get(paramName);
    }
    //add start 2020/09/07
    public Name getProcessName() {
        return processName;
    }

    public void setProcessName(Name processName) {
        this.processName = processName;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isInclude() {
        return include;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }
    //add end 2020/09/07
}
