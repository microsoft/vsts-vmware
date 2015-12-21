var gulp = require("gulp");
var rimraf = require("rimraf");
var tsb = require("gulp-tsb");
var mocha = require("gulp-mocha");
var tslint = require("gulp-tslint");
var istanbul = require("gulp-istanbul");
var path = require("path");
var shell = require('shelljs')
var gutil = require('gulp-util');

var buildDirectory = "_build";
var packageDirectory = __dirname + "/_package";
var sourcePaths = {
    typescriptFiles: "src/**/*.ts",
    copyFiles: ["src/*.json", "src/*.md", "src/Images/*", "src/Tasks/**/*.json", "src/Tasks/**/*.js", "src/Tasks/**/*.md", "src/Tasks/**/*.png", "src/Tasks/**/*.svg"]       
};
var testPaths = {
    typescriptFiles: "tests/**/*.ts",
    copyfiles: ["tests/**/*.js"],
    compiledJSFiles: buildDirectory + "/**/*Tests*.js"  
};

var jsCoverageDir = path.join(buildDirectory, "codecoverage");

// create and keep compiler
var compilation = tsb.create({
    target: 'es5',
    module: 'commonjs',
    declaration: false,
    verbose: false
});

gulp.task("compile", ["lint"], function() {
     return gulp.src([sourcePaths.typescriptFiles, testPaths.typescriptFiles], { base: "." })
        .pipe(compilation())
        .pipe(gulp.dest(__dirname))
        .pipe(istanbul({includeUntested: true}))
        .pipe(istanbul.hookRequire());
});

gulp.task("build", ["compile"], function() {
    var copyFiles = sourcePaths.copyFiles.concat(testPaths.copyfiles);    
    return gulp.src(copyFiles, { base: "." })        
        .pipe(gulp.dest(buildDirectory));
});

gulp.task("lint", function() {
    return gulp.src([sourcePaths.typescriptFiles, testPaths.typescriptFiles])
        .pipe(tslint())
        .pipe(tslint.report("verbose"))
});

gulp.task("clean", function(done) {
    return rimraf(buildDirectory, function() {
        // rimraf deletes the directory asynchronously
        done();
    });
});

gulp.task("test", ["build"], function() {
    return gulp.src(testPaths.compiledJSFiles, { read: false })
        .pipe(mocha())
        .pipe(istanbul.writeReports({
            dir: jsCoverageDir,
            reportOpts: { dir: jsCoverageDir }
        }))
        .pipe(istanbul.enforceThresholds({ thresholds: { global: 95 } }));
});

gulp.task("testci", ["build"], function() {
    return gulp.src(testPaths.compiledJSFiles, { read: false })
        .pipe(mocha({ reporter: "xunit", reporterOptions: { output: path.join(buildDirectory, "mochaTestResult.xml") } }))
        .pipe(istanbul.writeReports({
            dir: jsCoverageDir,
            reportOpts: { dir: jsCoverageDir }
        }))
        .pipe(istanbul.enforceThresholds({ thresholds: { global: 95 } }));
});

gulp.task("package", ["build"], function() {    
    shell.cd(buildDirectory + "/src");
    if(shell.exec("tfx extension create --manifest-globs vss-extension.json --output-path " + packageDirectory).code !== 0) {
        throw new gutil.PluginError({
            plugin: "package",
            message: "Unable to create package."          
        });
    }   
});

gulp.task("watch", function() {
    gulp.watch([sourcePaths.typescriptFiles, testPaths.typescriptFiles], ["test"]);    
});

gulp.task("default", ["build"]);
