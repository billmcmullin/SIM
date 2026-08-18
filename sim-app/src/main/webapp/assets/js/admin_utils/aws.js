// assets/js/admin_utils/aws.js
(function () {
    'use strict';
    window.AdminPage = window.AdminPage || {};

    window.AdminPage.Aws = {
        init
    };

    function init(config) {
        const contextPath = config?.contextPath || '';

        const testBtn = document.getElementById('testAwsConnectionBtn');
        if (testBtn) {
            testBtn.addEventListener('click', () => testAwsConnection(contextPath));
        }

        const saveBtn = document.getElementById('saveAwsConfigBtn');
        if (saveBtn) {
            saveBtn.addEventListener('click', () => saveAwsConfiguration(contextPath));
        }

        const restartBtn = document.getElementById('restartEc2InstanceBtn');
        if (restartBtn) {
            restartBtn.addEventListener('click', () => restartEc2Instance(contextPath));
        }
    }

    async function testAwsConnection(contextPath) {
        const resultEl = document.getElementById('awsEc2Result');
        const btn = document.getElementById('testAwsConnectionBtn');
        const data = readAwsForm();

        if (!data.awsRegion || !data.awsInstanceId) {
            setResult(resultEl, 'AWS region and EC2 instance ID are required for testing.', false);
            return;
        }

        const originalText = btn ? btn.textContent : '';
        setButtonState(btn, true, 'Testing...');

        try {
            const params = buildParams(data);
            const { payload, ok } = await window.AdminPage.Api.postUrlEncoded(
                `${contextPath}/admin/test-aws-connection`,
                params
            );

            if (ok && payload?.status === 'ok') {
                const stateSuffix = payload?.instanceState ? ` (state: ${payload.instanceState})` : '';
                const sourceSuffix = payload?.credentialSource ? ` [source: ${payload.credentialSource}]` : '';
                setResult(resultEl, `AWS connection successful${stateSuffix}.${sourceSuffix}`, true);
            } else {
                const sourceSuffix = payload?.credentialSource ? ` [source: ${payload.credentialSource}]` : '';
                const categorySuffix = payload?.errorCategory ? ` [category: ${payload.errorCategory}]` : '';
                const codeSuffix = payload?.errorCode ? ` [code: ${payload.errorCode}]` : '';
                const requestIdSuffix = payload?.requestId ? ` [requestId: ${payload.requestId}]` : '';
                setResult(
                    resultEl,
                    (payload?.message || 'AWS connection test failed.')
                    + sourceSuffix
                    + categorySuffix
                    + codeSuffix
                    + requestIdSuffix,
                    false
                );
            }
        } catch (err) {
            setResult(resultEl, `AWS connection error: ${err.message}`, false);
        } finally {
            setButtonState(btn, false, originalText || 'Test AWS Connection');
        }
    }

    async function saveAwsConfiguration(contextPath) {
        const resultEl = document.getElementById('awsEc2Result');
        const btn = document.getElementById('saveAwsConfigBtn');
        const data = readAwsForm();

        if (!data.awsRegion || !data.awsInstanceId) {
            setResult(resultEl, 'AWS region and EC2 instance ID are required.', false);
            return;
        }

        const originalText = btn ? btn.textContent : '';
        setButtonState(btn, true, 'Saving...');

        try {
            const params = buildParams(data);
            const { payload, ok } = await window.AdminPage.Api.postUrlEncoded(
                `${contextPath}/admin/save-config`,
                params
            );

            if (ok && payload?.status === 'ok') {
                setResult(resultEl, 'AWS configuration saved.', true);

                const awsAccessKeyIdNote = document.getElementById('awsAccessKeyIdStoredNote');
                if (awsAccessKeyIdNote) {
                    awsAccessKeyIdNote.style.display = 'block';
                }

                const awsSecretAccessKeyNote = document.getElementById('awsSecretAccessKeyStoredNote');
                if (awsSecretAccessKeyNote) {
                    awsSecretAccessKeyNote.style.display = 'block';
                }

                const awsAccessKeyId = document.getElementById('awsAccessKeyId');
                if (awsAccessKeyId) {
                    awsAccessKeyId.value = '';
                }

                const awsSecretAccessKey = document.getElementById('awsSecretAccessKey');
                if (awsSecretAccessKey) {
                    awsSecretAccessKey.value = '';
                }
            } else {
                setResult(resultEl, payload?.message || 'Unable to save AWS configuration.', false);
            }
        } catch (err) {
            setResult(resultEl, `Save error: ${err.message}`, false);
        } finally {
            setButtonState(btn, false, originalText || 'Save AWS Configuration');
        }
    }

    async function restartEc2Instance(contextPath) {
        const resultEl = document.getElementById('awsEc2Result');
        const btn = document.getElementById('restartEc2InstanceBtn');
        const data = readAwsForm();

        if (!data.awsRegion || !data.awsInstanceId) {
            setResult(resultEl, 'AWS region and EC2 instance ID are required for restart.', false);
            return;
        }

        const sureMessage = `Are you sure you want to restart EC2 instance ${data.awsInstanceId} in region ${data.awsRegion}?`;
        const confirmed = confirmAction(sureMessage);
        if (!confirmed) {
            setResult(resultEl, 'EC2 restart canceled.', false);
            return;
        }

        const originalText = btn ? btn.textContent : '';
        setButtonState(btn, true, 'Restarting...');

        try {
            const params = buildRestartParams(data, true);
            const { payload, ok } = await window.AdminPage.Api.postUrlEncoded(
                `${contextPath}/admin/aws/restart-ec2`,
                params
            );

            if (ok && payload?.status === 'ok') {
                const target = payload?.instanceId || data.awsInstanceId;
                setResult(resultEl, `EC2 reboot request submitted for ${target}.`, true);
            } else {
                setResult(resultEl, payload?.message || 'EC2 restart request failed.', false);
            }
        } catch (err) {
            setResult(resultEl, `Restart error: ${err.message}`, false);
        } finally {
            setButtonState(btn, false, originalText || 'Restart EC2 Instance');
        }
    }

    function readAwsForm() {
        return {
            awsRegion: document.getElementById('awsRegion')?.value.trim() || '',
            awsInstanceId: document.getElementById('awsInstanceId')?.value.trim() || '',
            awsAccessKeyId: document.getElementById('awsAccessKeyId')?.value.trim() || '',
            awsSecretAccessKey: document.getElementById('awsSecretAccessKey')?.value.trim() || ''
        };
    }

    function buildParams(data) {
        const params = new URLSearchParams();
        params.append('awsRegion', data.awsRegion || '');
        params.append('awsInstanceId', data.awsInstanceId || '');
        params.append('awsAccessKeyId', data.awsAccessKeyId || '');
        params.append('awsSecretAccessKey', data.awsSecretAccessKey || '');
        return params;
    }

    function buildRestartParams(data, restartConfirmed) {
        const params = buildParams(data);
        params.append('restartConfirmed', restartConfirmed ? 'true' : 'false');
        return params;
    }

    function setResult(el, message, success) {
        if (!el) {
            return;
        }
        el.textContent = message;
        el.style.color = success ? '#047857' : '#b91c1c';
    }

    function setButtonState(btn, disabled, text) {
        if (!btn) {
            return;
        }
        btn.disabled = disabled;
        if (typeof text === 'string') {
            btn.textContent = text;
        }
    }

    function confirmAction(message) {
        const confirmFn = window.confirm?.bind(window);
        return typeof confirmFn === 'function' ? confirmFn(message) : false;
    }
})();
