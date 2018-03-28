node {
    def win = System.properties['os.name'].startsWith('Windows')

    stage 'Checkout'
    checkout scm

    gradleProjects = ['disassembler']

    for (proj in gradleProjects) {
        dir(proj) {
            stage "Build ${proj}"
            def ver = version()
            echo "Building ${proj} version ${ver} on branch ${env.BRANCH_NAME}"
            if (win) {
                bat "./gradlew -PBUILD_NUMBER=${env.BUILD_NUMBER}"
            } else {
                sh "./gradlew -PBUILD_NUMBER=${env.BUILD_NUMBER}"
            }

            stage "Archive ${proj}"
            artifactPath = proj == 'relay' ? 'build/*.exe' : 'build/libs/*.jar'
            step([$class: 'ArtifactArchiver', artifacts: artifactPath, excludes: 'build/libs/*-base.jar',
                  fingerprint: true])
        }
    }
}

def version() {
  def matcher = readFile('build.gradle') =~ 'version = \'(.+)\''
  matcher ? matcher[0][1] : null
}
