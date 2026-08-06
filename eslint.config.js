export default [
    {
        files: ["sim-app/src/main/webapp/assets/js/**/*.js"],
        ignores: ["**/node_modules/**", "**/target/**", "**/*.min.js"],
        linterOptions: {
            reportUnusedDisableDirectives: "warn"
        },
        languageOptions: {
            ecmaVersion: "latest",
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
            "no-global-assign": "error",
            "no-throw-literal": "error",
            "no-return-assign": ["error", "always"],
            "eqeqeq": ["warn", "always"],
            "no-implicit-globals": "error",
            "no-fallthrough": "error",
            "no-constant-condition": ["error", { "checkLoops": false }],
            "no-empty": ["error", { "allowEmptyCatch": true }],
            "valid-typeof": "error",
            "no-dupe-keys": "error",
            "no-self-compare": "error",
            "radix": "error",
            "no-eval": "error",
            "no-implied-eval": "error",
            "no-new-func": "error",
            "no-script-url": "error",
            "no-proto": "error",
            "no-caller": "error",
            "no-with": "error",
            "no-alert": "warn",
            "array-callback-return": "warn",
            "default-case-last": "warn",
            "dot-notation": "warn",
            "no-else-return": "warn",
            "no-implicit-coercion": "warn",
            "no-multi-assign": "warn",
            "no-param-reassign": "warn",
            "no-shadow": "warn",
            "no-use-before-define": ["warn", { "functions": false, "classes": true, "variables": true }],
            "no-useless-concat": "warn",
            "no-useless-return": "warn",
            "no-var": "warn",
            "object-shorthand": "warn",
            "prefer-arrow-callback": "warn",
            "prefer-const": "warn",
            "prefer-rest-params": "warn",
            "prefer-spread": "warn",
            "prefer-template": "warn",
            "curly": ["warn", "all"],
            "consistent-return": "warn",
            "no-debugger": "error"
        }
    },
    {
        files: [
            "sim-app/src/main/webapp/assets/js/**/widget_review*.js",
            "sim-app/src/main/webapp/assets/js/widget_review_util/**/*.js"
        ],
        languageOptions: {
            ecmaVersion: "latest",
            sourceType: "module"
        }
    }
];
