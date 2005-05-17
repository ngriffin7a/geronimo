/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.security.cert.X509Certificate;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @version $Rev:  $ $Date:  $
 */
public class CertificateCallbackHandler implements ClearableCallbackHandler {
    X509Certificate certificate;
    public CertificateCallbackHandler(X509Certificate certificate) {
        this.certificate = certificate;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            Callback callback = callbacks[i];
            if (callback instanceof CertificateCallback) {
                CertificateCallback cc = (CertificateCallback) callback;
                cc.setCertificate(certificate);
            } else {
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    public void clear() {
        certificate = null;
    }
}
