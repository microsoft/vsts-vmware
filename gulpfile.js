var gulp = require("gulp");
var rimraf = require("rimraf");
var tsb = require("gulp-tsb");
var mocha = require("gulp-mocha");
var tslint = require("gulp-tslint");
var istanbul = require("gulp-istanbul");
var path = require("path");

var buildDirectory = "_build";
var sourceFiles = ["src/**/*.ts", "tests/**/*.ts"];
var testFiles = [buildDirectory + "/**/*Tests*.js"];
var jsCoverageDir = path.join(buildDirectory, "codecoverage");

// create and keep compiler
var compilation = tsb.create({
    target: 'es5',
    module: 'commonjs',
    declaration: false,
    verbose: false
});

gulp.task("build", ["lint"], function() {
    return gulp.src(sourceFiles, { base: "." })
        .pipe(compilation())
        .pipe(gulp.dest(buildDirectory))
        .pipe(istanbul({includeUntested: true}))
        .pipe(istanbul.hookRequire());
});

gulp.task("lint", function() {
    return gulp.src(sourceFiles)
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
    return gulp.src(testFiles, { read: false })
        .pipe(mocha())
        .pipe(istanbul.writeReports({
            dir: jsCoverageDir,
            reportOpts: { dir: jsCoverageDir }
        }))
        .pipe(istanbul.enforceThresholds({ thresholds: { global: 95 } }));
});

gulp.task("testci", ["build"], function() {
    return gulp.src(testFiles, { read: false })
        .pipe(mocha({ reporter: "xunit", reporterOptions: { output: path.join(buildDirectory, "mochaTestResult.xml") } }))
        .pipe(istanbul.writeReports({
            dir: jsCoverageDir,
            reportOpts: { dir: jsCoverageDir }
        }))
        .pipe(istanbul.enforceThresholds({ thresholds: { global: 95 } }));
});

gulp.task("watch", function() {
    gulp.watch(sourceFiles, ["test"]);    
});

gulp.task("default", ["build"]);
