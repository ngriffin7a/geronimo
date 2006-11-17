<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.security.cert.X509Certificate" %>
<%@ page import="org.apache.geronimo.ca.helper.util.CAHelperUtils"%>
<%
    X509Certificate cert = (X509Certificate) CAHelperUtils.getCertificateStore().getCACertificate();
    request.setAttribute("cert", cert);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Download CA's Certificate</title>
</head>
<body>
<h2>Download CA's Certificate</h2>
<p>This page enables you to download and install CA's certificate into your web browser. Click on the link below to
download and install CA's certificate.</p>

<a href="DownloadCertificateServlet?type=ca">Download CA's Certificate</a> &nbsp; <a href="<%=request.getContextPath()%>">Back to CA Helper home</a>

    <table border="0">
        <tr>
            <th colspan="2" align="left">Certificate Details</th>
        </tr>
        <tr>
            <th align="right">Version:</th>
            <td>${cert.version}</td>
        </tr>
        <tr>
            <th align="right">Subject:</th>
            <td>${cert.subjectDN.name}</td>
        </tr>
        <tr>
            <th align="right">Issuer:</th>
            <td>${cert.issuerDN.name}</td>
        </tr>
        <tr>
            <th align="right">Serial Number:</th>
            <td>${cert.serialNumber}</td>
        </tr>
        <tr>
            <th align="right">Valid From:</th>
            <td>${cert.notBefore}</td>
        </tr>
        <tr>
            <th align="right">Valid To:</th>
            <td>${cert.notAfter}</td>
        </tr>
        <tr>
            <th align="right">Signature Alg:</th>
            <td>${cert.sigAlgName}</td>
        </tr>
        <tr>
            <th align="right">Public Key Alg:</th>
            <td>${cert.publicKey.algorithm}</td>
        </tr>
        <tr>
            <th align="right" valign="top">cert.toString()</th>
            <td><pre>${cert}</pre></td>
        </tr>
    </table>

</body>
</html>
