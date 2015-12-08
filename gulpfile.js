var gulp = require("gulp");
var rimraf = require("rimraf");
var tsb = require("gulp-tsb");
var mocha = require("gulp-mocha");
var tslint = require("gulp-tslint");

var buildDirectory = "_build";
var sourceFiles = ["src/**/*.ts", "tests/**/*.ts"];
var testFiles = [buildDirectory + "/**/*Tests*.js"];

// create and keep compiler
var compilation = tsb.create({
    target: 'es5',
    module: 'commonjs',
    declaration: false
});

gulp.task("build", ["clean", "lint"], function() {
    return gulp.src(sourceFiles, { base: "." })
        .pipe(compilation())
        .pipe(gulp.dest(buildDirectory));
});

gulp.task("lint", ["clean"], function() {
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
        .pipe(mocha());
});

gulp.task("testci", ["build"], function() {
    return gulp.src(testFiles, { read: false })
        .pipe(mocha({ reporter: 'xunit', reporterOptions: { output: '_build/testTaskMochaTestResult.xml'} }));
});

gulp.task("watch", function() {
    gulp.watch(sourceFiles, ["test"]);    
});

gulp.task("default", ["build"]);

