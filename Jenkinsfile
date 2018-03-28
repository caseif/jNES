node {
    def win = System.properties['os.name'].startsWith('Windows')

    stage 'Checkout'
    checkout scm

    stage "Build"
    def ver = version()
    echo "Building jNES version ${ver} on branch ${env.BRANCH_NAME}"
    if (win) {
        bat "./gradlew -PBUILD_NUMBER=${env.BUILD_NUMBER}"
    } else {
        sh "./gradlew -PBUILD_NUMBER=${env.BUILD_NUMBER}"
    }

    stage "Archive"
    step([$class: 'ArtifactArchiver', artifacts: artifactPath, excludes: 'build/libs/*-base.jar', fingerprint: true])
}

def version() {
    def matcher = readFile('build.gradle') =~ 'version = \'(.+)\''
    matcher ? matcher[0][1] : null
}
