export default [
    {
        files: ["sim-app/src/main/webapp/assets/js/**/*.js"],
        ignores: ["**/node_modules/**", "**/target/**"],
        languageOptions: {
            ecmaVersion: 2021,
            sourceType: "script",
            globals: {
                window: "readonly",
                document: "readonly",
                console: "readonly",
                fetch: "readonly",
                URL: "readonly",
                URLSearchParams: "readonly",
                FormData: "readonly",
                history: "readonly",
                localStorage: "readonly",
                alert: "readonly",
                confirm: "readonly",
                Chart: "readonly",
                setTimeout: "readonly"
            }
        },
        rules: {
            "no-unused-vars": "warn",
            "no-undef": "error",
            "no-redeclare": "error",
            "no-unreachable": "error",
            "eqeqeq": ["warn", "always"],
            "no-implicit-globals": "error",
            "no-fallthrough": "error",
            "no-constant-condition": ["error", { "checkLoops": false }],
            "no-empty": ["error", { "allowEmptyCatch": true }],
            "valid-typeof": "error",
            "no-dupe-keys": "error",
            "no-self-compare": "error",
            "curly": ["warn", "all"],
            "consistent-return": "warn",
            "prefer-const": "warn",
            "no-debugger": "error"
        }
    },
    {
        files: ["sim-app/src/main/webapp/assets/js/**/payload_builder.js"],
        languageOptions: {
            sourceType: "module"
        }
    }
];
