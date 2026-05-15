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
                fetch: "readonly"
            }
        },
        rules: {
            "no-unused-vars": "warn",
            "no-undef": "error",
            "no-redeclare": "error",
            "no-unreachable": "error",
            "eqeqeq": ["warn", "always"]
        }
    }
];
