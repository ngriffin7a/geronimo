/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.util.asn1.x509;

import org.apache.geronimo.util.asn1.ASN1Encodable;
import org.apache.geronimo.util.asn1.ASN1EncodableVector;
import org.apache.geronimo.util.asn1.DERInteger;
import org.apache.geronimo.util.asn1.DERObjectIdentifier;
import org.apache.geronimo.util.asn1.DERSequence;
import org.apache.geronimo.util.asn1.DERGeneralizedTime;
import org.apache.geronimo.util.asn1.DERBitString;
import org.apache.geronimo.util.asn1.DERSet;

/**
 * Generator for Version 2 AttributeCertificateInfo
 * <pre>
 * AttributeCertificateInfo ::= SEQUENCE {
 *       version              AttCertVersion -- version is v2,
 *       holder               Holder,
 *       issuer               AttCertIssuer,
 *       signature            AlgorithmIdentifier,
 *       serialNumber         CertificateSerialNumber,
 *       attrCertValidityPeriod   AttCertValidityPeriod,
 *       attributes           SEQUENCE OF Attribute,
 *       issuerUniqueID       UniqueIdentifier OPTIONAL,
 *       extensions           Extensions OPTIONAL
 * }
 * </pre>
 *
 */
public class V2AttributeCertificateInfoGenerator
{
    private DERInteger version;
    private Holder holder;
    private AttCertIssuer issuer;
    private AlgorithmIdentifier signature;
    private DERInteger serialNumber;
    private AttCertValidityPeriod attrCertValidityPeriod;
    private ASN1EncodableVector attributes;
    private DERBitString issuerUniqueID;
    private X509Extensions extensions;
    private DERGeneralizedTime startDate, endDate;

    public V2AttributeCertificateInfoGenerator()
    {
        this.version = new DERInteger(1);
        attributes = new ASN1EncodableVector();
    }

    public void setHolder(Holder holder)
    {
        this.holder = holder;
    }

    public void addAttribute(String oid, ASN1Encodable value)
    {
        attributes.add(new Attribute(new DERObjectIdentifier(oid), new DERSet(value)));
    }

    /**
     * @param attribute
     */
    public void addAttribute(Attribute attribute)
    {
        attributes.add(attribute);
    }

    public void setSerialNumber(
        DERInteger  serialNumber)
    {
        this.serialNumber = serialNumber;
    }

    public void setSignature(
        AlgorithmIdentifier    signature)
    {
        this.signature = signature;
    }

    public void setIssuer(
        AttCertIssuer    issuer)
    {
        this.issuer = issuer;
    }

    public void setStartDate(
        DERGeneralizedTime startDate)
    {
        this.startDate = startDate;
    }

    public void setEndDate(
        DERGeneralizedTime endDate)
    {
        this.endDate = endDate;
    }

    public void setIssuerUniqueID(
        DERBitString    issuerUniqueID)
    {
        this.issuerUniqueID = issuerUniqueID;
    }

    public void setExtensions(
        X509Extensions    extensions)
    {
        this.extensions = extensions;
    }

    public AttributeCertificateInfo generateAttributeCertificateInfo()
    {
        if ((serialNumber == null) || (signature == null)
            || (issuer == null) || (startDate == null) || (endDate == null)
            || (holder == null) || (attributes == null))
        {
            throw new IllegalStateException("not all mandatory fields set in V2 AttributeCertificateInfo generator");
        }

        ASN1EncodableVector  v = new ASN1EncodableVector();

        v.add(version);
        v.add(holder);
        v.add(issuer);
        v.add(signature);
        v.add(serialNumber);

        //
        // before and after dates => AttCertValidityPeriod
        //
        AttCertValidityPeriod validity = new AttCertValidityPeriod(startDate, endDate);
        v.add(validity);

        // Attributes
        v.add(new DERSequence(attributes));

        if (issuerUniqueID != null)
        {
            v.add(issuerUniqueID);
        }

        if (extensions != null)
        {
            v.add(extensions);
        }

        return new AttributeCertificateInfo(new DERSequence(v));
    }
}
