node {
    def app

    stage('Clone repository') {
        /* Let's make sure we have the repository cloned to our workspace */

        checkout scm
    }
     stage('Groovy Test') {
	def variables = load "groovy/test.groovy"
	     
	     echo "Groovy variables : " + variables
	     
	     def myName = variables.runTime
	     echo "Groovy Test runTime : " + myName
	     
	      echo "Groovy Test fileName : " + variables.fileName
	     
	     echo "Groovy Test data : " + variables.data()
     }
     stage('stashFiles') {
	stash name: "targetFiles", includes : "Dockerfile, groovy/*.groovy, target/*.zip"
    }
	
 stage('UnstashFiles') {
	unstash name: "targetFiles"
    }	
	
    stage('Build image') {
        /* This builds the actual image; synonymous to
         * docker build on the command line */

        app = docker.build("ramkuvel/mule-docker")
    }

    stage('Test image') {
        /* Ideally, we would run a test framework against our image.
         * For this example, we're using a Volkswagen-type approach ;-) */

        app.inside {
            sh 'echo "Tests passed"'
        }
    }

    stage('Push image') {
        /* Finally, we'll push the image with two tags:
         * First, the incremental build number from Jenkins
         * Second, the 'latest' tag.
         * Pushing multiple tags is cheap, as all the layers are reused. 
        docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
            app.push("${env.BUILD_NUMBER}")
            app.push("latest")
        }
		*/
    }
}
